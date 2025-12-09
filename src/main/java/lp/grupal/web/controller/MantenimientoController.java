package lp.grupal.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lp.grupal.web.model.Empresa;
import lp.grupal.web.model.Usuario;
import lp.grupal.web.model.dao.IEmpresaDAO;
import lp.grupal.web.model.dao.IUsuarioDAO;

@Controller
@RequestMapping("/admin/mantenimiento")
public class MantenimientoController {

    @Autowired private IEmpresaDAO empresaDAO;
    @Autowired private IUsuarioDAO usuarioDAO;

    // Helper para verificar si es Gerente (Admin Vendedor)
    private boolean esGerente(Usuario u) {
        return u.getTipoUsuario().getNombreRol().equals("ADMIN") || 
               u.getTipoUsuario().getNombreRol().equals("ADMIN_VENDEDOR");
    }

    // 1. MENU PRINCIPAL (HUB)
    @GetMapping
    public String index(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";
        
        model.addAttribute("usuario", usuario);
        // ESTO CARGA TU HTML CON LAS TARJETAS (mantenimiento.html)
        return "empresa/mantenimiento"; 
    }

    // 2. PERFIL (Todos los empleados pueden ver esto)
    @GetMapping("/perfil")
    public String verPerfil(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        model.addAttribute("usuario", usuario);
        return "empresa/perfil"; // Asegúrate que el archivo se llame perfil.html en templates/empresa/
    }

    @PostMapping("/perfil/guardar")
    public String guardarPerfil(@ModelAttribute Usuario usuarioForm, HttpSession session, RedirectAttributes flash) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        try {
            Usuario usuarioBD = usuarioDAO.findById(usuarioSesion.getIdusuario()).get();
            usuarioBD.setNombres(usuarioForm.getNombres());
            usuarioBD.setTelefono(usuarioForm.getTelefono());
            if (usuarioForm.getContrasena() != null && !usuarioForm.getContrasena().isEmpty()) {
                usuarioBD.setContrasena(usuarioForm.getContrasena());
            }
            usuarioDAO.save(usuarioBD);
            session.setAttribute("usuario", usuarioBD);
            flash.addFlashAttribute("exito", "Perfil actualizado.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error actualizando perfil.");
        }
        return "redirect:/admin/mantenimiento/perfil";
    }

    // 3. EMPRESA (Solo Gerentes)
    @GetMapping("/empresa")
    public String verEmpresa(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";
        
        // Si no es gerente, lo mandamos al menú principal, NO a perfil
        if (!esGerente(usuario)) return "redirect:/admin/mantenimiento";

        Empresa miEmpresa = usuario.getEmpresa();
        if (miEmpresa == null) miEmpresa = empresaDAO.findById(1).orElse(new Empresa());

        model.addAttribute("usuario", usuario);
        model.addAttribute("empresa", miEmpresa);
        return "empresa/empresa"; // Asegúrate que el archivo se llame empresa.html
    }

    @PostMapping("/empresa/guardar")
    public String guardarEmpresa(@ModelAttribute Empresa empresa, HttpSession session, RedirectAttributes flash) {
        Usuario u = (Usuario) session.getAttribute("usuario");
        if (!esGerente(u)) return "redirect:/admin/mantenimiento";

        try {
            empresaDAO.save(empresa);
            u.setEmpresa(empresa); // Actualizar sesión
            session.setAttribute("usuario", u);
            flash.addFlashAttribute("exito", "Empresa actualizada.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/mantenimiento/empresa";
    }
}