package lp.grupal.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import lp.grupal.web.model.DetalleVenta;
import lp.grupal.web.model.Producto;
import lp.grupal.web.model.Servicio; // NUEVO
import lp.grupal.web.model.TipoComprobante;
import lp.grupal.web.model.TipoDocumento;
import lp.grupal.web.model.TipoUsuario;
import lp.grupal.web.model.Usuario;
import lp.grupal.web.model.Venta;
import lp.grupal.web.model.dao.IDetalleVentaDAO;
import lp.grupal.web.model.dao.IProductoDAO;
import lp.grupal.web.model.dao.IServicioDAO; // NUEVO
import lp.grupal.web.model.dao.ITipoComprobanteDAO;
import lp.grupal.web.model.dao.ITipoDocumentoDAO;
import lp.grupal.web.model.dao.ITipoUsuarioDAO;
import lp.grupal.web.model.dao.IUsuarioDAO;
import lp.grupal.web.model.dao.IVentaDAO;

@Controller
@RequestMapping("/admin/pos")
public class PosController {

    @Autowired private IProductoDAO productoDAO;
    @Autowired private IServicioDAO servicioDAO; // 1. INYECCIÓN DEL DAO DE SERVICIOS
    @Autowired private IUsuarioDAO usuarioDAO;
    @Autowired private IVentaDAO ventaDAO;
    @Autowired private IDetalleVentaDAO detalleVentaDAO;
    @Autowired private ITipoComprobanteDAO tipoComprobanteDAO; 
    @Autowired private ITipoUsuarioDAO tipoUsuarioDAO; 
    @Autowired private ITipoDocumentoDAO tipoDocumentoDAO;

    // --- 1. VISTA PRINCIPAL DEL POS ---
    @GetMapping
    public String index(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        // Obtener ID de la empresa del usuario logueado
        Integer idEmpresa = usuario.getEmpresa().getIdempresa();

        // Cargar listas filtradas por empresa para los selectores
        List<Producto> productos = productoDAO.findByEmpresa(idEmpresa);
        
        // 2. CARGAR SERVICIOS TAMBIÉN
        List<Servicio> servicios = servicioDAO.findByEmpresa(idEmpresa); 
        
        List<Usuario> clientes = usuarioDAO.listarClientesPorEmpresa(idEmpresa);
        List<TipoComprobante> comprobantes = tipoComprobanteDAO.findByEmpresa(idEmpresa);
        
        // Cargar tipos de documento activos (DNI, RUC) para el modal de nuevo cliente
        List<TipoDocumento> tiposDoc = tipoDocumentoDAO.findByEstadoTrue();

        // Enviar todo al HTML
        model.addAttribute("productos", productos);
        model.addAttribute("servicios", servicios); // ENVIAR AL FRONTEND
        model.addAttribute("clientes", clientes);
        model.addAttribute("comprobantes", comprobantes);
        model.addAttribute("tiposDoc", tiposDoc);
        model.addAttribute("usuario", usuario);

        return "empresa/pos";
    }

    // --- 2. API: GUARDAR CLIENTE RÁPIDO (AJAX) ---
    // (Este método se mantiene igual que tu versión original)
    @PostMapping("/cliente/guardar")
    @ResponseBody
    public ResponseEntity<?> guardarClienteRapido(@RequestBody Map<String, String> data, HttpSession session) {
        Usuario vendedor = (Usuario) session.getAttribute("usuario");
        if (vendedor == null) return ResponseEntity.status(401).body("Sesión expirada");

        try {
            String numDoc = data.get("numDoc");

            // Validar existencia
            Usuario existente = usuarioDAO.findByNumeroDocumento(numDoc);
            if (existente != null) {
                return ResponseEntity.badRequest().body("{\"message\": \"El cliente con documento " + numDoc + " ya existe.\"}");
            }

            // Validar datos mínimos
            if (data.get("idTipoDoc") == null || data.get("idTipoDoc").isEmpty()) {
                 throw new Exception("Seleccione un tipo de documento.");
            }

            Integer idTipoDoc = Integer.parseInt(data.get("idTipoDoc"));
            TipoDocumento tipoDocBD = tipoDocumentoDAO.findById(idTipoDoc).orElse(null);
            
            if (tipoDocBD == null) throw new Exception("Tipo de documento inválido.");

            if (numDoc.length() < tipoDocBD.getLongitudMin() || numDoc.length() > tipoDocBD.getLongitudMax()) {
                throw new Exception("El documento debe tener entre " + tipoDocBD.getLongitudMin() + " y " + tipoDocBD.getLongitudMax() + " dígitos.");
            }

            // Crear Cliente
            Usuario nuevo = new Usuario();
            nuevo.setEmpresa(vendedor.getEmpresa());
            
            TipoUsuario rolCliente = tipoUsuarioDAO.findById(2).orElse(null); // ID 2 = CLIENTE
            nuevo.setTipoUsuario(rolCliente);
            
            nuevo.setTipoDocumento(tipoDocBD);
            nuevo.setNumeroDocumento(numDoc);
            nuevo.setNombres(data.get("nombres"));
            nuevo.setRazonSocial(data.get("razonSocial"));
            nuevo.setDireccionFiscal(data.get("direccion"));
            nuevo.setTelefono(data.get("telefono"));
            nuevo.setEmail(data.get("email"));
            
            nuevo.setUsername(numDoc); 
            nuevo.setContrasena("CLIENTE_POS"); 
            nuevo.setEstado(1); 

            Usuario guardado = usuarioDAO.save(nuevo);
            return ResponseEntity.ok(guardado);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"Error: " + e.getMessage() + "\"}");
        }
    }

    // --- 3. API: PROCESAR VENTA (AJAX) ---
    // (AQUÍ ESTÁ LA LÓGICA IMPORTANTE MODIFICADA)
    @PostMapping("/procesar")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> procesarVenta(@RequestBody Map<String, Object> payload, HttpSession session) {
        Usuario vendedor = (Usuario) session.getAttribute("usuario");
        if (vendedor == null) return ResponseEntity.status(401).body("Sesión expirada");

        try {
            // Obtener datos del JSON
            Integer idCliente = Integer.parseInt(payload.get("idCliente").toString());
            Integer idTipoComp = Integer.parseInt(payload.get("idTipoComprobante").toString());
            Double totalVenta = Double.parseDouble(payload.get("total").toString());
            
            // "items" ahora traerá el campo "tipo" ('PRODUCTO' o 'SERVICIO')
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");

            // A. Gestionar Comprobante
            TipoComprobante tipoComp = tipoComprobanteDAO.findById(idTipoComp)
                    .orElseThrow(() -> new Exception("Tipo de comprobante no encontrado"));
            
            int nuevoCorrelativo = tipoComp.getCorrelativoActual() + 1;
            String numeroComprobanteGenerado = String.format("%08d", nuevoCorrelativo);
            
            tipoComp.setCorrelativoActual(nuevoCorrelativo);
            tipoComprobanteDAO.save(tipoComp);

            // B. Crear Venta
            Venta venta = new Venta();
            venta.setUsuario(usuarioDAO.findById(idCliente).orElse(null));
            venta.setEmpleado(vendedor);
            venta.setEmpresa(vendedor.getEmpresa());
            venta.setMonto_total(totalVenta);
            venta.setEstado("COMPLETADO");
            
            venta.setTipoComprobante(tipoComp);
            venta.setSerie_comprobante(tipoComp.getSerieDefault());
            venta.setNum_comprobante(numeroComprobanteGenerado);
            
            Venta ventaGuardada = ventaDAO.save(venta);

            // C. Guardar Detalles (PRODUCTOS O SERVICIOS)
            for (Map<String, Object> item : items) {
                Integer idItem = Integer.parseInt(item.get("id").toString());
                Integer cantidad = Integer.parseInt(item.get("cantidad").toString());
                Double precio = Double.parseDouble(item.get("precio").toString());
                
                // Recibimos el tipo desde el JS
                String tipo = item.get("tipo").toString(); 

                DetalleVenta detalle = new DetalleVenta();
                detalle.setVenta(ventaGuardada);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(precio); 
                
                // ASIGNACIÓN CONDICIONAL
                if ("PRODUCTO".equals(tipo)) {
                    Producto prod = productoDAO.findById(idItem).orElse(null);
                    detalle.setProducto(prod);
                    // IMPORTANTE: Al setear producto, tu trigger de BD descontará stock
                } else {
                    Servicio serv = servicioDAO.findById(idItem).orElse(null);
                    detalle.setServicio(serv);
                    // Al ser servicio, no afecta stock
                }
                
                detalleVentaDAO.save(detalle);
            }

            return ResponseEntity.ok(Map.of("mensaje", "Venta registrada", "idVenta", ventaGuardada.getIdventa()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error procesando venta: " + e.getMessage());
        }
    }
}