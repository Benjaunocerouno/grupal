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

@Controller
@RequestMapping("/admin/empresa")
public class EmpresaController {

    @Autowired
    private IEmpresaDAO empresaDAO;

    // Mostrar formulario de edición de la empresa
    @GetMapping("/configuracion")
    public String verConfiguracion(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        // Validación de seguridad básica
        if (usuario == null || !esGerente(usuario)) {
            return "redirect:/admin/dashboard";
        }

        // Obtener la empresa del usuario logueado
        Empresa miEmpresa = usuario.getEmpresa();
        
        // Si por alguna razón es nula, cargamos la 1 o creamos una vacía
        if (miEmpresa == null) {
            miEmpresa = empresaDAO.findById(1).orElse(new Empresa());
        } else {
            // Refrescamos desde BD para tener los datos más recientes
            miEmpresa = empresaDAO.findById(miEmpresa.getIdempresa()).orElse(miEmpresa);
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("empresa", miEmpresa);
        
        // Retorna la vista: src/main/resources/templates/empresa/empresa.html
        return "empresa/empresa"; 
    }

    // Guardar cambios
    @PostMapping("/guardar")
    public String guardarEmpresa(@ModelAttribute Empresa empresa, HttpSession session, RedirectAttributes flash) {
        Usuario u = (Usuario) session.getAttribute("usuario");
        
        if (u == null || !esGerente(u)) {
            return "redirect:/login";
        }

        try {
            // Aseguramos que solo edite SU empresa (Evitar inyección de ID)
            if(!empresa.getIdempresa().equals(u.getEmpresa().getIdempresa())) {
                throw new Exception("No tienes permiso para editar esta empresa.");
            }

            empresaDAO.save(empresa);
            
            // Actualizamos la sesión para que el cambio de nombre/logo se vea al instante
            u.setEmpresa(empresa); 
            session.setAttribute("usuario", u);
            
            flash.addFlashAttribute("exito", "Información de la empresa actualizada correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        
        return "redirect:/admin/empresa/configuracion";
    }

    // Helper para verificar roles permitidos (Admin o Gerente)
    private boolean esGerente(Usuario u) {
        String rol = u.getTipoUsuario().getNombreRol();
        return rol.equals("ADMIN") || rol.equals("ADMIN_VENDEDOR");
    }
}