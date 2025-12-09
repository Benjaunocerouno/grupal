package lp.grupal.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lp.grupal.web.model.Caja;
import lp.grupal.web.model.DetallePedido;
import lp.grupal.web.model.DetalleVenta;
import lp.grupal.web.model.Pedido;
import lp.grupal.web.model.TipoComprobante;
import lp.grupal.web.model.Usuario;
import lp.grupal.web.model.Venta;
import lp.grupal.web.model.dao.ICajaDAO;
import lp.grupal.web.model.dao.IDetalleVentaDAO;
import lp.grupal.web.model.dao.IPedidoDAO;
import lp.grupal.web.model.dao.ITipoComprobanteDAO;
import lp.grupal.web.model.dao.IVentaDAO;

@Controller
@RequestMapping("/admin/pedidos-web")
public class PedidoAdminController {

    @Autowired private IPedidoDAO pedidoDAO;
    @Autowired private IVentaDAO ventaDAO;
    @Autowired private IDetalleVentaDAO detalleVentaDAO;
    @Autowired private ICajaDAO cajaDAO;
    @Autowired private ITipoComprobanteDAO tipoComprobanteDAO;

    // 1. LISTAR PEDIDOS
    @GetMapping
    public String listarPedidosPendientes(Model model, HttpSession session) {
        if (!validarAcceso(session)) return "redirect:/login";

        Usuario u = (Usuario) session.getAttribute("usuario");
        Integer idEmpresa = u.getEmpresa().getIdempresa();

        List<Pedido> pedidosPendientes = pedidoDAO.findByEmpresaAndEstadoOrderByFechaPedidoDesc(idEmpresa, "PENDIENTE");
        
        model.addAttribute("pedidos", pedidosPendientes);
        return "empresa/pedidos_web"; 
    }

    // 2. REVISAR Y PRE-LLENAR DATOS (LÓGICA MEJORADA)
    @GetMapping("/revisar/{id}")
    public String revisarPedido(@PathVariable("id") Integer idPedido, Model model, HttpSession session, RedirectAttributes flash) {
        if (!validarAcceso(session)) return "redirect:/login";

        Pedido pedido = pedidoDAO.findById(idPedido).orElse(null);
        if (pedido == null || !pedido.getEstado().equals("PENDIENTE")) {
            flash.addFlashAttribute("error", "El pedido no existe o ya fue atendido.");
            return "redirect:/admin/pedidos-web";
        }

        model.addAttribute("pedido", pedido);
        
        // --- DETECCIÓN INTELIGENTE DE COMPROBANTE ---
        Usuario cliente = pedido.getUsuario();
        String tipoSugerido = "BOLETA"; // Default
        
        if (cliente.getTipoDocumento() != null) {
            String abrev = cliente.getTipoDocumento().getAbreviatura();
            // Si es RUC (por abreviatura o longitud de documento)
            if ("RUC".equals(abrev) || (cliente.getNumeroDocumento() != null && cliente.getNumeroDocumento().length() == 11)) {
                tipoSugerido = "FACTURA";
            }
        }
        model.addAttribute("tipoSugerido", tipoSugerido);
        // --------------------------------------------

        // Cargar tipos de comprobante
        Usuario u = (Usuario) session.getAttribute("usuario");
        List<TipoComprobante> tipos = tipoComprobanteDAO.findByEmpresa(u.getEmpresa().getIdempresa());
        model.addAttribute("tiposComprobante", tipos);
        
        return "empresa/atender_pedido"; 
    }

    // 3. RECHAZAR PEDIDO (NUEVO MÉTODO)
    @GetMapping("/rechazar/{id}")
    public String rechazarPedido(@PathVariable("id") Integer idPedido, HttpSession session, RedirectAttributes flash) {
        if (!validarAcceso(session)) return "redirect:/login";

        Pedido pedido = pedidoDAO.findById(idPedido).orElse(null);
        if (pedido != null && pedido.getEstado().equals("PENDIENTE")) {
            pedido.setEstado("RECHAZADO");
            pedido.setMotivoRechazo("Cancelado por administración");
            pedidoDAO.save(pedido);
            flash.addFlashAttribute("exito", "Pedido #" + idPedido + " rechazado correctamente.");
        } else {
            flash.addFlashAttribute("error", "No se pudo rechazar el pedido.");
        }
        return "redirect:/admin/pedidos-web";
    }

    // 4. PROCESAR VENTA (CON TRANSACCIÓN)
    @PostMapping("/procesar")
    @Transactional
    public String procesarVenta(@RequestParam("idPedido") Integer idPedido,
                                @RequestParam("tipoComprobante") Integer idTipoComprobante, 
                                HttpSession session, 
                                RedirectAttributes flash) {
        
        Usuario usuarioAdmin = (Usuario) session.getAttribute("usuario");
        if (usuarioAdmin == null) return "redirect:/login";
        
        try {
            Pedido pedido = pedidoDAO.findById(idPedido).orElse(null);
            
            if (pedido == null || !pedido.getEstado().equals("PENDIENTE")) { 
                 flash.addFlashAttribute("error", "Error: El pedido ya no está pendiente.");
                 return "redirect:/admin/pedidos-web";
            }

            TipoComprobante tipoComp = tipoComprobanteDAO.findById(idTipoComprobante).orElse(null);
            if(tipoComp == null) throw new Exception("Tipo de comprobante inválido");

            int nuevoCorr = tipoComp.getCorrelativoActual() + 1;
            String numGenerado = String.format("%08d", nuevoCorr);
            
            tipoComp.setCorrelativoActual(nuevoCorr);
            tipoComprobanteDAO.save(tipoComp);

            Venta nuevaVenta = new Venta();
            nuevaVenta.setUsuario(pedido.getUsuario());
            nuevaVenta.setEmpleado(usuarioAdmin);
            nuevaVenta.setEmpresa(usuarioAdmin.getEmpresa()); 
            nuevaVenta.setEstado("COMPLETADO");
            nuevaVenta.setMonto_total(pedido.getMontoEstimado());
            nuevaVenta.setDireccionEnvio(pedido.getObservacionesCliente()); 
            
            nuevaVenta.setTipoComprobante(tipoComp);
            nuevaVenta.setSerie_comprobante(tipoComp.getSerieDefault());
            nuevaVenta.setNum_comprobante(numGenerado); 
            
            Venta ventaGuardada = ventaDAO.save(nuevaVenta);
            
            for (DetallePedido dp : pedido.getDetalles()) {
                 DetalleVenta dv = new DetalleVenta();
                 dv.setVenta(ventaGuardada);
                 dv.setProducto(dp.getProducto());
                 dv.setCantidad(dp.getCantidad());
                 dv.setPrecioUnitario(dp.getPrecioReferencial());
                 detalleVentaDAO.save(dv);
            }
            
            pedido.setEstado("APROBADO");
            pedido.setIdVentaGenerada(ventaGuardada.getIdventa());
            pedido.setIdVendedorRevisor(usuarioAdmin.getIdusuario());
            pedidoDAO.save(pedido);

            Caja movimiento = new Caja();
            movimiento.setTipoMovimiento("INGRESO");
            movimiento.setConcepto("Venta Web #" + ventaGuardada.getIdventa());
            movimiento.setMonto(pedido.getMontoEstimado());
            movimiento.setMetodoPago("WEB/TRANSFERENCIA"); 
            movimiento.setVenta(ventaGuardada);
            movimiento.setPedido(pedido);
            movimiento.setEmpresa(usuarioAdmin.getEmpresa());
            movimiento.setUsuarioResponsable(usuarioAdmin);
            
            cajaDAO.save(movimiento);

            flash.addFlashAttribute("exito", "Venta procesada. Ticket: " + numGenerado);

        } catch (Exception e) {
            e.printStackTrace();
            flash.addFlashAttribute("error", "Error al procesar: " + e.getMessage());
        }

        return "redirect:/admin/pedidos-web";
    }

    private boolean validarAcceso(HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuario");
        return u != null && (u.getTipoUsuario().getNombreRol().equals("ADMIN") || 
                             u.getTipoUsuario().getNombreRol().equals("VENDEDOR") || 
                             u.getTipoUsuario().getNombreRol().equals("ADMIN_VENDEDOR"));
    }
}