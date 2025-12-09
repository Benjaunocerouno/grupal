package lp.grupal.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping; // Importa todo para asegurar
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lp.grupal.web.model.Proveedor;
import lp.grupal.web.model.Usuario;
import lp.grupal.web.model.dao.IProveedorDAO;

@Controller
@RequestMapping("/admin/proveedores")
public class ProveedorController {

    @Autowired private IProveedorDAO proveedorDAO;

    private boolean esGerente(Usuario u) {
        return u != null && (u.getTipoUsuario().getNombreRol().equals("ADMIN") || 
               u.getTipoUsuario().getNombreRol().equals("ADMIN_VENDEDOR"));
    }

    @GetMapping
    public String index(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";
        
        // CORRECCIÓN 1: Si no es gerente, mandar al MENÚ, no a la misma página
        if (!esGerente(usuario)) return "redirect:/admin/mantenimiento";

        List<Proveedor> lista = proveedorDAO.findByActivoTrue();
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("proveedores", lista);
        model.addAttribute("nuevoProveedor", new Proveedor());
        
        // Asegúrate que tu archivo se llame proveedores.html en templates/empresa/
        return "empresa/proveedores"; 
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Proveedor proveedor, HttpSession session, RedirectAttributes flash) {
        Usuario u = (Usuario) session.getAttribute("usuario");
        
        // CORRECCIÓN 2: Redirigir al menú si no hay permiso
        if (!esGerente(u)) return "redirect:/admin/mantenimiento";

        try {
            proveedor.setEmpresa(u.getEmpresa());
            if(proveedor.getActivo() == null) proveedor.setActivo(true);
            proveedorDAO.save(proveedor);
            flash.addFlashAttribute("exito", "Proveedor guardado.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/proveedores";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Integer id, HttpSession session, RedirectAttributes flash) {
        Usuario u = (Usuario) session.getAttribute("usuario");
        
        // CORRECCIÓN 3: Redirigir al menú si no hay permiso
        if (!esGerente(u)) return "redirect:/admin/mantenimiento";

        Proveedor p = proveedorDAO.findById(id).orElse(null);
        if (p != null) {
            p.setActivo(false);
            proveedorDAO.save(p);
            flash.addFlashAttribute("exito", "Proveedor eliminado.");
        }
        return "redirect:/admin/proveedores";
    }
}