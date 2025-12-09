package lp.grupal.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession; // Importar HttpSession
import lp.grupal.web.model.Empresa;
import lp.grupal.web.model.Usuario; // Importar Usuario
import lp.grupal.web.model.dao.IEmpresaDAO;

@Controller
public class ContactoController {
    
    @Autowired
    private IEmpresaDAO empresaDAO;
    
    @GetMapping("/contacto")
    // AGREGAR HttpSession session
    public String contacto(Model model, HttpSession session) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        // 1. DETERMINAR QUÉ EMPRESA VISUALIZAR
        Integer idEmpresaAVisualizar = 1; // Por defecto: Empresa 1 para invitados
        
        // Si hay usuario logueado (cliente, vendedor, etc.)
        if (usuario != null && usuario.getEmpresa() != null) {
            idEmpresaAVisualizar = usuario.getEmpresa().getIdempresa();
        } 
        // Lógica de contexto: si un visitante eligió ver la empresa 2
        else if (session.getAttribute("idEmpresaVisualizar") != null) {
            idEmpresaAVisualizar = (Integer) session.getAttribute("idEmpresaVisualizar");
        }
        
        // 2. BUSCAR LA EMPRESA POR EL ID DETERMINADO
        // Corregido: Ya no usa findById(1) hardcodeado
        Empresa miEmpresa = empresaDAO.findById(idEmpresaAVisualizar).orElse(new Empresa());
        
        model.addAttribute("empresa", miEmpresa);

        return "contacto";
    }
    
    @PostMapping("/contacto/enviar")
    public String enviarMensaje(
            @RequestParam("nombre") String nombre,
            @RequestParam("email") String email,
            @RequestParam("asunto") String asunto,
            @RequestParam("mensaje") String mensaje,
            RedirectAttributes redirectAttributes) {
                
        // Nota: Si quieres saber a qué empresa va dirigido el mensaje, 
        // también deberías pasar el idEmpresa en un campo oculto desde el HTML.

        System.out.println("=== NUEVO MENSAJE DE CONTACTO ===");
        System.out.println("De: " + nombre + " (" + email + ")");
        System.out.println("Asunto: " + asunto);
        System.out.println("Mensaje: " + mensaje);
        System.out.println("=================================");
        
        redirectAttributes.addFlashAttribute("mensajeExito", "¡Gracias por escribirnos! Hemos recibido tu mensaje y te responderemos pronto.");

        return "redirect:/contacto";
    }
}