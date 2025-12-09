package lp.grupal.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpSession;

@Controller
public class EmpresaContextController {

    // Permite cambiar la empresa que se visualiza en la tienda pública
    // Ejemplo de uso: <a href="/visitar/1">Ver Tienda 1</a>
    @GetMapping("/visitar/{id}")
    public String cambiarContextoEmpresa(@PathVariable("id") Integer idEmpresa, HttpSession session) {
        
        // Guardamos en la sesión la preferencia del usuario
        // "Quiero ver los productos de la empresa X"
        session.setAttribute("idEmpresaVisualizar", idEmpresa);
        
        // Redirigimos al inicio para que cargue los productos de esa empresa
        return "redirect:/";
    }
}