package lp.grupal.web.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lp.grupal.web.model.Empresa;
import lp.grupal.web.model.Servicio; // Necesario para cargar datos de la empresa
import lp.grupal.web.model.SolicitudServicio;
import lp.grupal.web.model.Usuario;
import lp.grupal.web.model.dao.IEmpresaDAO;
import lp.grupal.web.model.dao.IServicioDAO;
import lp.grupal.web.model.dao.ISolicitudServicioDAO;

@Controller
public class ServicioController {

    @Autowired private IServicioDAO servicioDAO;
    @Autowired private ISolicitudServicioDAO solicitudServicioDAO;
    @Autowired private IEmpresaDAO empresaDAO; // Inyectar DAO de Empresa

    // Listar servicios (Página Pública)
    @GetMapping("/servicio")
    public String servicio(Model model, HttpSession session) {
        
        // 1. Determinar ID de Empresa (Igual que en Tienda e Inicio)
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        Integer idEmpresa = 1; // Default

        if (usuario != null) {
            idEmpresa = usuario.getEmpresa().getIdempresa();
        } else if (session.getAttribute("idEmpresaVisualizar") != null) {
            idEmpresa = (Integer) session.getAttribute("idEmpresaVisualizar");
        }

        try {
            // 2. Cargar Datos de la Empresa (Para el Logo/Nombre dinámico)
            Empresa datosEmpresa = empresaDAO.findById(idEmpresa).orElse(new Empresa());
            if (datosEmpresa.getNombre() == null) datosEmpresa.setNombre("TechSolutions");

            // 3. Cargar Servicios FILTRADOS por Empresa
            // CAMBIO CLAVE: Usamos findByEmpresa en lugar de findByActivoTrue
            List<Servicio> servicios = servicioDAO.findByEmpresa(idEmpresa);

            // 4. Enviar a la vista
            model.addAttribute("listaServicios", servicios);
            model.addAttribute("empresa", datosEmpresa);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("listaServicios", new ArrayList<>());
            model.addAttribute("empresa", new Empresa());
        }

        return "servicio";
    }

    // Mostrar formulario de agendar (Requiere Login)
    @GetMapping("/servicio/agendar/{id}")
    public String mostrarFormularioAgendar(@PathVariable("id") Integer idServicio, HttpSession session, Model model, RedirectAttributes flash) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        if (usuario == null) {
            flash.addFlashAttribute("error", "Debes iniciar sesión para agendar una cita.");
            return "redirect:/login";
        }

        Servicio servicio = servicioDAO.findById(idServicio).orElse(null);
        
        // Validación extra: Que el servicio pertenezca a la misma empresa del usuario
        if (servicio == null || !servicio.getEmpresa().getIdempresa().equals(usuario.getEmpresa().getIdempresa())) {
            return "redirect:/servicio";
        }

        // Cargar empresa también aquí por si la necesitas en el header del formulario
        model.addAttribute("empresa", usuario.getEmpresa());
        model.addAttribute("servicio", servicio);
        
        return "cliente/agendar_servicio"; 
    }

    // Procesar el formulario
    @PostMapping("/servicio/guardar")
    public String guardarSolicitud(@RequestParam("idServicio") Integer idServicio,
                                   @RequestParam("dispositivo") String dispositivo,
                                   @RequestParam("problema") String problema,
                                   @RequestParam("fechaCita") String fechaCitaStr,
                                   HttpSession session,
                                   RedirectAttributes flash) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        try {
            Servicio servicioBase = servicioDAO.findById(idServicio).orElse(null);
            if (servicioBase == null) throw new Exception("Servicio no encontrado.");

            SolicitudServicio solicitud = new SolicitudServicio();
            solicitud.setUsuario(usuario);
            solicitud.setServicioBase(servicioBase);
            solicitud.setEmpresa(servicioBase.getEmpresa()); // Asignar empresa del servicio
            solicitud.setDispositivoNombre(dispositivo);
            solicitud.setDescripcionProblema(problema);
            solicitud.setFechaCita(LocalDateTime.parse(fechaCitaStr)); 
            solicitud.setEstado("PENDIENTE");

            solicitudServicioDAO.save(solicitud);
            flash.addFlashAttribute("exito", "¡Cita agendada correctamente!");
            
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/servicio";
    }
}