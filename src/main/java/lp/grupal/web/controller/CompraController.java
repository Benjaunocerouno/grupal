package lp.grupal.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lp.grupal.web.model.Compra;
import lp.grupal.web.model.Usuario;
import lp.grupal.web.model.dao.ICategoriaDAO;
import lp.grupal.web.model.dao.ICompraDAO;
import lp.grupal.web.model.dao.IProductoDAO;
import lp.grupal.web.model.dao.IProveedorDAO;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.ArrayList;
import lp.grupal.web.model.DetalleCompra;
import lp.grupal.web.model.dao.IDetalleCompraDAO;

@Controller
@RequestMapping("/admin/compras") // <--- AQUI ESTA LA MAGIA
public class CompraController {

    @Autowired private IDetalleCompraDAO detalleCompraDAO;
    @Autowired private IProductoDAO productoDAO;
    @Autowired private ICategoriaDAO categoriaDAO;

    @Autowired private ICompraDAO compraDAO;
    @Autowired private IProveedorDAO proveedorDAO;


    private boolean esGerente(Usuario u) {
        return u != null && (u.getTipoUsuario().getNombreRol().equals("ADMIN") || 
               u.getTipoUsuario().getNombreRol().equals("ADMIN_VENDEDOR"));
    }


    @PostMapping("/guardar-api")
    @ResponseBody
    @Transactional // Si falla algo, no se guarda nada
    public ResponseEntity<?> guardarCompraApi(@RequestBody Map<String, Object> payload, HttpSession session) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return ResponseEntity.status(401).body("Sesión expirada");

        try {
            // 1. EXTRAER DATOS DE CABECERA
            Integer idProveedor = Integer.parseInt(payload.get("idProveedor").toString());
            String numComprobante = payload.get("numeroComprobante").toString();
            Double montoTotal = Double.parseDouble(payload.get("montoTotal").toString());
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");

            if(items.isEmpty()) throw new Exception("La compra no tiene productos.");

            // 2. CREAR Y GUARDAR LA COMPRA (CABECERA)
            Compra compra = new Compra();
            compra.setEmpresa(usuario.getEmpresa());
            compra.setUsuario(usuario); // Responsable
            compra.setProveedor(proveedorDAO.findById(idProveedor).orElseThrow(() -> new Exception("Proveedor no existe")));
            compra.setNumeroComprobante(numComprobante);
            compra.setMontoTotal(montoTotal);
            compra.setEstado("COMPLETADO");

            Compra compraGuardada = compraDAO.save(compra);

            // 3. GUARDAR LOS DETALLES
            for (Map<String, Object> item : items) {
                DetalleCompra det = new DetalleCompra();
                det.setCompra(compraGuardada);
                
                Integer idProd = Integer.parseInt(item.get("idProducto").toString());
                det.setProducto(productoDAO.findById(idProd).orElseThrow(() -> new Exception("Producto ID " + idProd + " no encontrado")));
                
                det.setCantidad(Integer.parseInt(item.get("cantidad").toString()));
                det.setCostoUnitario(Double.parseDouble(item.get("costo").toString()));

                detalleCompraDAO.save(det);
                // NOTA: El Trigger 'trg_aumentar_stock_compra' en BD actualizará el stock automáticamente
            }

            return ResponseEntity.ok(Map.of("mensaje", "Compra registrada correctamente", "id", compraGuardada.getIdcompra()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al procesar compra: " + e.getMessage());
        }
    }



    @GetMapping
    public String index(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";
        if (!esGerente(usuario)) return "redirect:/admin/mantenimiento";

        Integer idEmpresa = usuario.getEmpresa().getIdempresa();

        // 1. Cargar listas existentes
        List<Compra> lista = compraDAO.findAllByOrderByFechaCompraDesc();
        
        // 2. NUEVO: Cargar datos para el formulario de compra y el modal
        model.addAttribute("productos", productoDAO.findByEmpresa(idEmpresa));
        model.addAttribute("categorias", categoriaDAO.findByEmpresa(idEmpresa)); // Para el modal
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("compras", lista);
        model.addAttribute("proveedores", proveedorDAO.findByActivoTrue());
        model.addAttribute("nuevaCompra", new Compra());
        
        return "empresa/compras";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Compra compra, HttpSession session, RedirectAttributes flash) {
        Usuario u = (Usuario) session.getAttribute("usuario");
        if (!esGerente(u)) return "redirect:/admin/mantenimiento";

        try {
            compra.setUsuario(u);
            compra.setEmpresa(u.getEmpresa());
            compraDAO.save(compra);
            flash.addFlashAttribute("exito", "Compra registrada.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/compras";
    }
}