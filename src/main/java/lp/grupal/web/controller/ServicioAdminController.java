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
import lp.grupal.web.model.Servicio;
import lp.grupal.web.model.Usuario;
import lp.grupal.web.model.dao.ICategoriaDAO;
import lp.grupal.web.model.dao.IServicioDAO;

@Controller
@RequestMapping("/admin/servicios")
public class ServicioAdminController {

    @Autowired private IServicioDAO servicioDAO;
    @Autowired private ICategoriaDAO categoriaDAO;

    // LISTAR (Solo de mi empresa)
    @GetMapping
    public String listar(Model model, @RequestParam(value = "buscar", required = false) String buscar, HttpSession session) {
        if (!validarAcceso(session)) return "redirect:/login";

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        Integer idEmpresa = usuario.getEmpresa().getIdempresa();

        List<Servicio> servicios;
        
        // CORRECCIÓN: Usar métodos filtrados por empresa
        if (buscar != null && !buscar.isEmpty()) {
            servicios = servicioDAO.buscarPorEmpresaYCriterio(idEmpresa, buscar);
        } else {
            servicios = servicioDAO.findByEmpresa(idEmpresa);
        }

        model.addAttribute("servicios", servicios);
        model.addAttribute("palabraClave", buscar);
        return "empresa/servicios_admin"; 
    }

    // NUEVO
    @GetMapping("/nuevo")
    public String nuevo(Model model, HttpSession session) {
        if (!validarAcceso(session)) return "redirect:/login";

        model.addAttribute("servicio", new Servicio());
        model.addAttribute("categorias", categoriaDAO.findByActivoTrue()); 
        model.addAttribute("titulo", "Nuevo Servicio");
        return "empresa/form_servicio"; 
    }

    // EDITAR (Validando propiedad)
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Integer id, Model model, HttpSession session, RedirectAttributes flash) {
        if (!validarAcceso(session)) return "redirect:/login";

        Servicio serv = servicioDAO.findById(id).orElse(null);
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        // Seguridad: Verificar que sea de mi empresa
        if (serv == null || !serv.getEmpresa().getIdempresa().equals(usuario.getEmpresa().getIdempresa())) {
            flash.addFlashAttribute("error", "Servicio no encontrado o acceso denegado.");
            return "redirect:/admin/servicios";
        }

        model.addAttribute("servicio", serv);
        model.addAttribute("categorias", categoriaDAO.findByActivoTrue());
        model.addAttribute("titulo", "Editar Servicio");
        return "empresa/form_servicio";
    }

    // GUARDAR
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Servicio servicio, RedirectAttributes flash, HttpSession session) {
        if (!validarAcceso(session)) return "redirect:/login";

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        try {
            // ASIGNAR EMPRESA
            servicio.setEmpresa(usuario.getEmpresa());
            
            if(servicio.getActivo() == null) servicio.setActivo(true);
            
            // Validación de categoría (para evitar nulos)
            if(servicio.getIdcategoria() == null) {
                 // Si tu entidad usa objeto Categoria, esto puede variar. 
                 // Asumiendo que el form envía el ID en un campo auxiliar o directo al objeto.
                 // Si usas th:field="*{categoria}", Spring lo bindearía.
            }

            servicioDAO.save(servicio);
            flash.addFlashAttribute("exito", "Servicio guardado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        return "redirect:/admin/servicios";
    }

    // ELIMINAR (LÓGICO)
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Integer id, RedirectAttributes flash, HttpSession session) {
        if (!validarAcceso(session)) return "redirect:/login";

        Servicio serv = servicioDAO.findById(id).orElse(null);
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (serv != null && serv.getEmpresa().getIdempresa().equals(usuario.getEmpresa().getIdempresa())) {
            serv.setActivo(false);
            servicioDAO.save(serv);
            flash.addFlashAttribute("exito", "Servicio eliminado.");
        } else {
            flash.addFlashAttribute("error", "No se pudo eliminar.");
        }
        return "redirect:/admin/servicios";
    }

    private boolean validarAcceso(HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuario");
        return u != null && (u.getTipoUsuario().getNombreRol().equals("ADMIN") || 
                             u.getTipoUsuario().getNombreRol().equals("VENDEDOR") ||
                             u.getTipoUsuario().getNombreRol().equals("ADMIN_VENDEDOR"));
    }
}