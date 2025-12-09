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
import lp.grupal.web.model.dao.ICompraDAO;
import lp.grupal.web.model.dao.IProveedorDAO;

@Controller
@RequestMapping("/admin/compras") // <--- AQUI ESTA LA MAGIA
public class CompraController {

    @Autowired private ICompraDAO compraDAO;
    @Autowired private IProveedorDAO proveedorDAO;

    private boolean esGerente(Usuario u) {
        return u != null && (u.getTipoUsuario().getNombreRol().equals("ADMIN") || 
               u.getTipoUsuario().getNombreRol().equals("ADMIN_VENDEDOR"));
    }

    @GetMapping
    public String index(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";
        if (!esGerente(usuario)) return "redirect:/admin/mantenimiento";

        List<Compra> lista = compraDAO.findAllByOrderByFechaCompraDesc();
        
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