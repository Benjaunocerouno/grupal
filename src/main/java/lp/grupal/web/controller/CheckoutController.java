package lp.grupal.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;

@Controller
public class CheckoutController {

    @GetMapping("/checkout")
    public String mostrarCheckout(HttpSession session) {
        
        if (session.getAttribute("usuario") == null) {
            return "redirect:/login";
        }
        return "cliente/checkout"; 
    }
}