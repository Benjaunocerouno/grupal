package lp.grupal.web.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import lp.grupal.web.model.DetallePedido;
import lp.grupal.web.model.Pedido;
import lp.grupal.web.model.Producto;
import lp.grupal.web.model.Usuario;
import lp.grupal.web.model.dao.IDetallePedidoDAO;
import lp.grupal.web.model.dao.IPedidoDAO;
import lp.grupal.web.model.dao.IProductoDAO;

@RestController // Usamos RestController porque respondemos JSON para el JavaScript
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired private IPedidoDAO pedidoDAO;
    @Autowired private IDetallePedidoDAO detallePedidoDAO;
    @Autowired private IProductoDAO productoDAO;

    @PostMapping("/guardar")
    @Transactional // Si falla un detalle, se cancela todo el pedido
    public ResponseEntity<Map<String, Object>> guardarPedido(@RequestBody Map<String, Object> payload, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. Validar Usuario Logueado
            Usuario cliente = (Usuario) session.getAttribute("usuario");
            if (cliente == null) {
                response.put("status", "error");
                response.put("message", "Debes iniciar sesión para comprar.");
                return ResponseEntity.status(401).body(response);
            }

            // 2. Extraer datos del JSON (Payload)
            String direccion = (String) payload.get("direccion");
            String ciudad = (String) payload.get("ciudad");
            String referencia = (String) payload.get("referencia");
            String telefono = (String) payload.get("telefono");
            String tipoPago = (String) payload.get("tipoPago");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> carrito = (List<Map<String, Object>>) payload.get("carrito");

            if (carrito == null || carrito.isEmpty()) {
                throw new Exception("El carrito está vacío.");
            }

            // 3. Crear Cabecera del Pedido
            Pedido pedido = new Pedido();
            pedido.setUsuario(cliente);
            pedido.setEmpresa(cliente.getEmpresa()); // El pedido es para la empresa a la que pertenece el cliente
            pedido.setFechaPedido(LocalDateTime.now());
            pedido.setEstado("PENDIENTE");
            
            // Guardamos la info de envío en observaciones (o puedes crear campos específicos si prefieres)
            String obs = String.format("Envío: %s, %s (Ref: %s). Tel: %s. Pago: %s", 
                                       direccion, ciudad, referencia, telefono, tipoPago);
            pedido.setObservacionesCliente(obs);

            // Calcular monto total
            double montoTotal = 0.0;

            // Guardamos primero el pedido para tener el ID (aunque sea con monto 0 temporalmente)
            pedido.setMontoEstimado(0.0);
            Pedido pedidoGuardado = pedidoDAO.save(pedido);

            // 4. Guardar Detalles
            for (Map<String, Object> item : carrito) {
                Integer idProd = Integer.parseInt(item.get("idproducto").toString());
                Integer cantidad = Integer.parseInt(item.get("cantidad").toString());
                Double precio = Double.parseDouble(item.get("precio").toString());

                Producto producto = productoDAO.findById(idProd).orElse(null);
                if (producto != null) {
                    DetallePedido detalle = new DetallePedido();
                    detalle.setPedido(pedidoGuardado);
                    detalle.setProducto(producto);
                    detalle.setCantidad(cantidad);
                    detalle.setPrecioReferencial(precio);
                    
                    detallePedidoDAO.save(detalle);
                    
                    montoTotal += (cantidad * precio);
                }
            }

            // 5. Actualizar el monto total real
            pedidoGuardado.setMontoEstimado(montoTotal);
            pedidoDAO.save(pedidoGuardado);

            // 6. Respuesta Exitosa
            response.put("status", "success");
            response.put("message", "¡Pedido #" + pedidoGuardado.getIdpedido() + " registrado con éxito!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error al procesar: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}