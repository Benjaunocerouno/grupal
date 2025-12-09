package lp.grupal.web.controller;

import org.springframework.beans.factory.annotation.Autowired; // Importar
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Importar Model
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import lp.grupal.web.model.dao.ITipoDocumentoDAO; // Importar DAO

@Controller
public class CheckoutController {

    @Autowired
    private ITipoDocumentoDAO tipoDocumentoDAO; // Inyectar DAO

    @GetMapping("/checkout")
    public String mostrarCheckout(HttpSession session, Model model) { // Agregar Model
        
        if (session.getAttribute("usuario") == null) {
            return "redirect:/login";
        }

        // Cargar los tipos de documento activos para el select
        model.addAttribute("tiposDoc", tipoDocumentoDAO.findByEstadoTrue());

        return "cliente/checkout"; 
    }
}