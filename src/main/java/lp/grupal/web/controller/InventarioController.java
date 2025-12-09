package lp.grupal.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lp.grupal.web.model.Producto;
import lp.grupal.web.model.Usuario;
import lp.grupal.web.model.dao.ICategoriaDAO;
import lp.grupal.web.model.dao.IProductoDAO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;

@Controller
@RequestMapping("/admin/inventario")
public class InventarioController {

    @Autowired private IProductoDAO productoDAO;
    @Autowired private ICategoriaDAO categoriaDAO;
    
    @PostMapping("/api/guardar-rapido")
    @ResponseBody
    public ResponseEntity<?> guardarProductoRapido(@RequestBody Producto producto, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return ResponseEntity.status(401).body("Sesión expirada");

        try {
            // Asignar empresa y valores por defecto
            producto.setEmpresa(usuario.getEmpresa());
            
            if (producto.getStock() == null) producto.setStock(0); // Nace con 0, la compra lo aumentará
            if (producto.getActivo() == null) producto.setActivo(true);
            if (producto.getImagenUrl() == null || producto.getImagenUrl().isEmpty()) {
                producto.setImagenUrl("/img/default-product.png");
            }

            // Guardar
            Producto guardado = productoDAO.save(producto);
            
            // Retornar solo lo necesario para el Select del HTML
            return ResponseEntity.ok(Map.of(
                "id", guardado.getIdproducto(),
                "nombre", guardado.getNombre(),
                "codigo", (guardado.getCodigoBarras() != null ? guardado.getCodigoBarras() : "")
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al guardar: " + e.getMessage());
        }
    }

    // LISTAR PRODUCTOS (Solo de mi empresa)
    @GetMapping
    public String listar(Model model, @RequestParam(value = "buscar", required = false) String buscar, HttpSession session) {
        if (!validarAcceso(session)) return "redirect:/login";

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        Integer idEmpresa = usuario.getEmpresa().getIdempresa();

        List<Producto> productos;
        
        // Usamos los métodos del DAO que filtran por ID EMPRESA
        if (buscar != null && !buscar.isEmpty()) {
            productos = productoDAO.buscarPorEmpresaYCriterio(idEmpresa, buscar);
        } else {
            productos = productoDAO.findByEmpresa(idEmpresa);
        }

        model.addAttribute("productos", productos);
        model.addAttribute("palabraClave", buscar);
        return "empresa/inventario";
    }

    // FORMULARIO NUEVO
    @GetMapping("/nuevo")
    public String nuevo(Model model, HttpSession session) {
        if (!validarAcceso(session)) return "redirect:/login";

        model.addAttribute("producto", new Producto());
        // Aquí podrías filtrar categorías por empresa si también fueran privadas
        model.addAttribute("categorias", categoriaDAO.findByActivoTrue()); 
        model.addAttribute("titulo", "Nuevo Producto");
        return "empresa/form_producto";
    }

    // FORMULARIO EDITAR
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, Model model, HttpSession session, RedirectAttributes flash) {
        if (!validarAcceso(session)) return "redirect:/login";

        Producto prod = productoDAO.findById(id).orElse(null);
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        // Seguridad: Verificar que el producto exista Y pertenezca a mi empresa
        if (prod == null || !prod.getEmpresa().getIdempresa().equals(usuario.getEmpresa().getIdempresa())) {
            flash.addFlashAttribute("error", "Producto no encontrado o acceso denegado.");
            return "redirect:/admin/inventario";
        }

        model.addAttribute("producto", prod);
        model.addAttribute("categorias", categoriaDAO.findByActivoTrue());
        model.addAttribute("titulo", "Editar Producto");
        return "empresa/form_producto";
    }

    // GUARDAR (CREAR O ACTUALIZAR)
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Producto producto, RedirectAttributes flash, HttpSession session) {
        if (!validarAcceso(session)) return "redirect:/login";

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        try {
            // IMPORTANTE: Asignar SIEMPRE el producto a la empresa del usuario actual
            producto.setEmpresa(usuario.getEmpresa());
            
            if(producto.getActivo() == null) producto.setActivo(true);
            
            productoDAO.save(producto);
            flash.addFlashAttribute("exito", "Producto guardado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        return "redirect:/admin/inventario";
    }

    // ELIMINAR (LÓGICO)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Integer id, RedirectAttributes flash, HttpSession session) {
        if (!validarAcceso(session)) return "redirect:/login";

        Producto prod = productoDAO.findById(id).orElse(null);
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        // Seguridad: Solo eliminar si es de mi empresa
        if (prod != null && prod.getEmpresa().getIdempresa().equals(usuario.getEmpresa().getIdempresa())) {
            prod.setActivo(false); 
            productoDAO.save(prod);
            flash.addFlashAttribute("exito", "Producto eliminado del catálogo.");
        } else {
            flash.addFlashAttribute("error", "No se pudo eliminar el producto.");
        }
        return "redirect:/admin/inventario";
    }

    // Helper para seguridad (Permite Admin, Vendedor, AdminVendedor)
    private boolean validarAcceso(HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuario");
        if (u == null) return false;
        String rol = u.getTipoUsuario().getNombreRol();
        return rol.equals("ADMIN") || rol.equals("VENDEDOR") || rol.equals("ADMIN_VENDEDOR");
    }
}