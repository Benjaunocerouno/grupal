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
    @Autowired private ITipoComprobanteDAO tipoComprobanteDAO; // NUEVO: Necesario para buscar el tipo

    // 1. LISTAR PEDIDOS
    @GetMapping
    public String listarPedidosPendientes(Model model, HttpSession session) {
        if (!validarAcceso(session)) return "redirect:/login";

        Usuario u = (Usuario) session.getAttribute("usuario");
        Integer idEmpresa = u.getEmpresa().getIdempresa();

        // Filtrar por empresa (Método nuevo que debes tener en el DAO)
        List<Pedido> pedidosPendientes = pedidoDAO.findByEmpresaAndEstadoOrderByFechaPedidoDesc(idEmpresa, "PENDIENTE");
        
        model.addAttribute("pedidos", pedidosPendientes);
        return "empresa/pedidos_web"; 
    }

    // 2. REVISAR Y PRE-LLENAR DATOS
    @GetMapping("/revisar/{id}")
    public String revisarPedido(@PathVariable("id") Integer idPedido, Model model, HttpSession session, RedirectAttributes flash) {
        if (!validarAcceso(session)) return "redirect:/login";

        Pedido pedido = pedidoDAO.findById(idPedido).orElse(null);
        if (pedido == null || !pedido.getEstado().equals("PENDIENTE")) {
            flash.addFlashAttribute("error", "El pedido no existe o ya fue atendido.");
            return "redirect:/admin/pedidos-web";
        }

        model.addAttribute("pedido", pedido);
        
        // Cargar tipos de comprobante de la empresa para el select
        Usuario u = (Usuario) session.getAttribute("usuario");
        List<TipoComprobante> tipos = tipoComprobanteDAO.findByEmpresa(u.getEmpresa().getIdempresa());
        model.addAttribute("tiposComprobante", tipos);
        
        return "empresa/atender_pedido"; 
    }

    // 3. PROCESAR VENTA (CON TRANSACCIÓN)
    @PostMapping("/procesar")
    @Transactional
    public String procesarVenta(@RequestParam("idPedido") Integer idPedido,
                                @RequestParam("idTipoComprobante") Integer idTipoComprobante, // Recibimos ID, no String
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

            // 1. Obtener y Actualizar Comprobante
            TipoComprobante tipoComp = tipoComprobanteDAO.findById(idTipoComprobante).orElse(null);
            if(tipoComp == null) throw new Exception("Tipo de comprobante inválido");

            int nuevoCorr = tipoComp.getCorrelativoActual() + 1;
            String numGenerado = String.format("%08d", nuevoCorr);
            
            tipoComp.setCorrelativoActual(nuevoCorr);
            tipoComprobanteDAO.save(tipoComp); // Guardar nuevo correlativo

            // 2. Crear VENTA
            Venta nuevaVenta = new Venta();
            nuevaVenta.setUsuario(pedido.getUsuario());
            nuevaVenta.setEmpleado(usuarioAdmin);
            nuevaVenta.setEmpresa(usuarioAdmin.getEmpresa()); // IMPORTANTE: Asignar empresa
            nuevaVenta.setEstado("COMPLETADO");
            nuevaVenta.setMonto_total(pedido.getMontoEstimado());
            
            // CORRECCIÓN 1: Método CamelCase
            nuevaVenta.setDireccionEnvio(pedido.getObservacionesCliente()); 
            
            // CORRECCIÓN 2: Asignar Objeto y Datos Generados
            nuevaVenta.setTipoComprobante(tipoComp);
            nuevaVenta.setSerie_comprobante(tipoComp.getSerieDefault());
            nuevaVenta.setNum_comprobante(numGenerado); 
            
            Venta ventaGuardada = ventaDAO.save(nuevaVenta);
            
            // 3. Copiar Detalles
            for (DetallePedido dp : pedido.getDetalles()) {
                 DetalleVenta dv = new DetalleVenta();
                 dv.setVenta(ventaGuardada);
                 dv.setProducto(dp.getProducto());
                 dv.setCantidad(dp.getCantidad());
                 dv.setPrecioUnitario(dp.getPrecioReferencial());
                 detalleVentaDAO.save(dv);
            }
            
            // 4. Actualizar Pedido
            pedido.setEstado("APROBADO");
            pedido.setIdVentaGenerada(ventaGuardada.getIdventa());
            pedido.setIdVendedorRevisor(usuarioAdmin.getIdusuario());
            pedidoDAO.save(pedido);

            // 5. Registrar Caja
            Caja movimiento = new Caja();
            movimiento.setTipoMovimiento("INGRESO");
            movimiento.setConcepto("Venta Web #" + ventaGuardada.getIdventa() + " - " + tipoComp.getNombre());
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