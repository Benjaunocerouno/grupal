package lp.grupal.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import lp.grupal.web.model.Caja;
import lp.grupal.web.model.DetalleVenta;
import lp.grupal.web.model.Producto;
import lp.grupal.web.model.Usuario;
import lp.grupal.web.model.Venta;
import lp.grupal.web.model.dao.ICajaDAO;
import lp.grupal.web.model.dao.IDetalleVentaDAO;
import lp.grupal.web.model.dao.IPedidoDAO;
import lp.grupal.web.model.dao.IProductoDAO;
import lp.grupal.web.model.dao.IVentaDAO;

@Controller
public class VentaController {

    @Autowired private IPedidoDAO pedidoDAO;
    @Autowired private IVentaDAO ventaDAO;
    @Autowired private IProductoDAO productoDAO;
    @Autowired private ICajaDAO cajaDAO;
    @Autowired private IDetalleVentaDAO detalleVentaDAO; // NECESARIO PARA GUARDAR DETALLES

    // --- CLASES DTO ---
    public static class ItemCarritoDTO {
        public Integer idproducto;
        public Integer cantidad;
        public Double precio; 
    }

    public static class SolicitudCompraDTO {
        public List<ItemCarritoDTO> carrito;
        public String direccion;
        public String ciudad;
        public String referencia;
        public String telefono;
        public String tipoPago;
    }

    // --- ENDPOINT 2: VENTA RÁPIDA (CHECKOUT) ---
    @PostMapping("/ventas/checkout")
    @ResponseBody 
    @Transactional
    public ResponseEntity<?> procesarVentaRapida(@RequestBody List<ItemCarritoDTO> carrito, HttpSession session) {
        
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
        if (usuarioLogueado == null) return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Debes iniciar sesión."));

        if (carrito == null || carrito.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Carrito vacío."));
        }

        // 1. Crear Venta
        Venta nuevaVenta = new Venta();
        nuevaVenta.setUsuario(usuarioLogueado);
        nuevaVenta.setEmpresa(usuarioLogueado.getEmpresa());
        nuevaVenta.setEstado("PAGADO");
        
        // CORRECCIÓN 1: Usar setDireccionEnvio (CamelCase)
        nuevaVenta.setDireccionEnvio("Retiro en Tienda (Venta Rápida)"); 
        
        // Guardamos venta para tener ID
        Venta ventaGuardada = ventaDAO.save(nuevaVenta);
        
        double totalCalculado = 0.0;

        // 2. Procesar Detalles
        for (ItemCarritoDTO item : carrito) {
            Producto prodBD = productoDAO.findById(item.idproducto).orElse(null);
            
            if (prodBD != null && prodBD.getActivo()) {
                if (prodBD.getStock() < item.cantidad) {
                    throw new RuntimeException("Sin stock: " + prodBD.getNombre());
                }

                DetalleVenta det = new DetalleVenta();
                det.setVenta(ventaGuardada);
                det.setProducto(prodBD);
                det.setCantidad(item.cantidad);
                det.setPrecioUnitario(prodBD.getPrecio()); // Asegúrate que sea setPrecioUnitario

                detalleVentaDAO.save(det); // Guardamos detalle en BD

                totalCalculado += (prodBD.getPrecio() * item.cantidad);
                
                // Restar stock (Si no tienes trigger)
                // prodBD.setStock(prodBD.getStock() - item.cantidad);
                // productoDAO.save(prodBD);
            }
        }

        // Actualizar total venta
        ventaGuardada.setMonto_total(totalCalculado);
        
        // CORRECCIÓN 2: ELIMINADA LA LÍNEA setDetalles QUE DABA ERROR
        // nuevaVenta.setDetalles(detalles); <--- ESTO NO ES NECESARIO SI GUARDAMOS CON DAO
        
        ventaDAO.save(ventaGuardada);

        // 3. Registrar Caja
        Caja movimiento = new Caja();
        movimiento.setTipoMovimiento("INGRESO");
        movimiento.setConcepto("Venta Rápida #" + ventaGuardada.getIdventa());
        movimiento.setMonto(totalCalculado);
        movimiento.setMetodoPago("EFECTIVO"); // Asumido
        movimiento.setVenta(ventaGuardada);
        movimiento.setEmpresa(usuarioLogueado.getEmpresa());
        
        cajaDAO.save(movimiento);

        return ResponseEntity.ok(Map.of("status", "success", "message", "Compra realizada! Ticket #" + ventaGuardada.getIdventa()));
    }
}