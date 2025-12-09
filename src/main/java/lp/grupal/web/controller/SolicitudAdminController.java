package lp.grupal.web.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lp.grupal.web.model.*;
import lp.grupal.web.model.dao.*;

@Controller
@RequestMapping("/admin/solicitudes-servicio")
public class SolicitudAdminController {

    @Autowired private ISolicitudServicioDAO solicitudDAO;
    @Autowired private IVentaDAO ventaDAO;
    @Autowired private IDetalleVentaDAO detalleVentaDAO;
    @Autowired private ICajaDAO cajaDAO;
    @Autowired private ITipoComprobanteDAO tipoComprobanteDAO;

    // 1. LISTAR SOLICITUDES PENDIENTES
    @GetMapping
    public String listar(Model model, HttpSession session) {
        if (!validarAcceso(session)) return "redirect:/login";

        Usuario u = (Usuario) session.getAttribute("usuario");
        // Usamos el nuevo método del DAO
        List<SolicitudServicio> lista = solicitudDAO.findByEmpresaAndEstadoOrderByFechaDesc(u.getEmpresa().getIdempresa(), "PENDIENTE");
        
        model.addAttribute("solicitudes", lista);
        return "empresa/solicitudes_servicio"; // Nombre del HTML de lista
    }

    // 2. REVISAR (CON LÓGICA DE COMPROBANTE INTELIGENTE)
    @GetMapping("/revisar/{id}")
    public String revisar(@PathVariable("id") Integer id, Model model, HttpSession session, RedirectAttributes flash) {
        if (!validarAcceso(session)) return "redirect:/login";

        SolicitudServicio solicitud = solicitudDAO.findById(id).orElse(null);
        if (solicitud == null || !solicitud.getEstado().equals("PENDIENTE")) {
            flash.addFlashAttribute("error", "Solicitud no encontrada o ya atendida.");
            return "redirect:/admin/solicitudes-servicio";
        }

        model.addAttribute("solicitud", solicitud);

        // --- DETECCIÓN INTELIGENTE (IGUAL QUE EN PEDIDOS) ---
        Usuario cliente = solicitud.getUsuario();
        String tipoSugerido = "BOLETA";
        
        if (cliente.getTipoDocumento() != null) {
            String abrev = cliente.getTipoDocumento().getAbreviatura();
            // Si es RUC o tiene 11 dígitos -> FACTURA
            if ("RUC".equals(abrev) || (cliente.getNumeroDocumento() != null && cliente.getNumeroDocumento().length() == 11)) {
                tipoSugerido = "FACTURA";
            }
        }
        model.addAttribute("tipoSugerido", tipoSugerido);
        // ----------------------------------------------------

        // Cargar comprobantes
        Usuario u = (Usuario) session.getAttribute("usuario");
        model.addAttribute("tiposComprobante", tipoComprobanteDAO.findByEmpresa(u.getEmpresa().getIdempresa()));

        return "empresa/atender_solicitud"; // Nombre del HTML de atender
    }

    // 3. RECHAZAR SOLICITUD
    @GetMapping("/rechazar/{id}")
    public String rechazar(@PathVariable("id") Integer id, HttpSession session, RedirectAttributes flash) {
        if (!validarAcceso(session)) return "redirect:/login";

        SolicitudServicio sol = solicitudDAO.findById(id).orElse(null);
        if (sol != null && sol.getEstado().equals("PENDIENTE")) {
            sol.setEstado("RECHAZADO");
            // sol.setDiagnosticoTecnico("Cancelado por administración"); 
            solicitudDAO.save(sol);
            flash.addFlashAttribute("exito", "Solicitud rechazada correctamente.");
        } else {
            flash.addFlashAttribute("error", "No se pudo rechazar.");
        }
        return "redirect:/admin/solicitudes-servicio";
    }

    // 4. PROCESAR (CREAR VENTA DE SERVICIO)
    @PostMapping("/procesar")
    @Transactional
    public String procesar(@RequestParam("idSolicitud") Integer idSolicitud,
                           @RequestParam("tipoComprobante") Integer idTipoComprobante,
                           @RequestParam("costoFinal") Double costoFinal, // Permitir ajustar precio
                           @RequestParam("diagnostico") String diagnostico,
                           HttpSession session, RedirectAttributes flash) {
        
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null) return "redirect:/login";

        try {
            SolicitudServicio sol = solicitudDAO.findById(idSolicitud).orElseThrow(() -> new Exception("Solicitud no válida"));
            if (!sol.getEstado().equals("PENDIENTE")) throw new Exception("Ya no está pendiente");

            // 1. Comprobante
            TipoComprobante tipoComp = tipoComprobanteDAO.findById(idTipoComprobante).orElseThrow(() -> new Exception("Tipo comp inválido"));
            int nuevoCorr = tipoComp.getCorrelativoActual() + 1;
            String numGenerado = String.format("%08d", nuevoCorr);
            tipoComp.setCorrelativoActual(nuevoCorr);
            tipoComprobanteDAO.save(tipoComp);

            // 2. Crear Venta
            Venta venta = new Venta();
            venta.setUsuario(sol.getUsuario());
            venta.setEmpleado(admin);
            venta.setEmpresa(admin.getEmpresa());
            venta.setEstado("COMPLETADO");
            venta.setMonto_total(costoFinal);
            venta.setTipoComprobante(tipoComp);
            venta.setSerie_comprobante(tipoComp.getSerieDefault());
            venta.setNum_comprobante(numGenerado);
            
            Venta ventaGuardada = ventaDAO.save(venta);

            // 3. Detalle Venta (Servicio)
            DetalleVenta det = new DetalleVenta();
            det.setVenta(ventaGuardada);
            det.setServicio(sol.getServicioBase()); // El servicio solicitado
            det.setCantidad(1);
            det.setPrecioUnitario(costoFinal);
            detalleVentaDAO.save(det);

            // 4. Actualizar Solicitud
            sol.setEstado("FINALIZADO"); // O "EN_PROCESO" si prefieres cobrar antes de arreglar
            sol.setIdVentaGenerada(ventaGuardada.getIdventa());
            sol.setDiagnosticoTecnico(diagnostico);
            solicitudDAO.save(sol);

            // 5. Caja
            Caja caja = new Caja();
            caja.setTipoMovimiento("INGRESO");
            caja.setConcepto("Servicio Web #" + sol.getIdsolicitud() + " - " + sol.getDispositivoNombre());
            caja.setMonto(costoFinal);
            caja.setMetodoPago("EFECTIVO/OTROS");
            caja.setVenta(ventaGuardada);
            caja.setSolicitudServicio(sol);
            caja.setEmpresa(admin.getEmpresa());
            caja.setUsuarioResponsable(admin);
            cajaDAO.save(caja);

            flash.addFlashAttribute("exito", "Servicio procesado. Ticket: " + numGenerado);

        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/solicitudes-servicio";
    }

    private boolean validarAcceso(HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuario");
        return u != null && (u.getTipoUsuario().getNombreRol().equals("ADMIN") || 
                             u.getTipoUsuario().getNombreRol().equals("VENDEDOR") || 
                             u.getTipoUsuario().getNombreRol().equals("ADMIN_VENDEDOR"));
    }
}