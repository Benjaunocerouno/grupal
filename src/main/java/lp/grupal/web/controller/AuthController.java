package lp.grupal.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired; // IMPORTAR MODEL
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession; // Importar List
import lp.grupal.web.model.Empresa;      
import lp.grupal.web.model.TipoUsuario;
import lp.grupal.web.model.Usuario; // IMPORTAR DAO EMPRESA
import lp.grupal.web.model.dao.IEmpresaDAO;
import lp.grupal.web.model.dao.ITipoUsuarioDAO;
import lp.grupal.web.model.dao.IUsuarioDAO; // IMPORTAR EMPRESA

@Controller
public class AuthController {

    @Autowired private IUsuarioDAO usuarioDAO;
    @Autowired private ITipoUsuarioDAO tipoUsuarioDAO;
    @Autowired private IEmpresaDAO empresaDAO; // INYECTAR

    // --- LOGIN ---
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam("email") String email,
                                @RequestParam("password") String password,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        
        Usuario usuario = usuarioDAO.findByEmailAndContrasena(email, password);

        if (usuario != null) {
            if (usuario.getEstado() == 0) {
                redirectAttributes.addFlashAttribute("error", "Tu cuenta está inactiva/bloqueada.");
                return "redirect:/login";
            }
            
            session.setAttribute("usuario", usuario); 
            
            String rol = usuario.getTipoUsuario().getNombreRol();
            if (rol.equals("ADMIN") || rol.equals("VENDEDOR") || rol.equals("ADMIN_VENDEDOR")) {
                return "redirect:/admin/dashboard"; 
            } else {
                return "redirect:/"; 
            }

        } else {
            redirectAttributes.addFlashAttribute("error", "Credenciales incorrectas.");
            return "redirect:/login";
        }
    }

    // --- REGISTRO (MODIFICADO) ---
    @GetMapping("/registro")
    public String registro(Model model) { // Agregar Model
        // Enviamos las empresas al select del HTML
        List<Empresa> empresas = empresaDAO.findAll();
        model.addAttribute("empresas", empresas);
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@RequestParam("nombres") String nombres,
                                   @RequestParam("username") String username,
                                   @RequestParam("email") String email,
                                   @RequestParam("password") String password,
                                   @RequestParam("telefono") String telefono,
                                   @RequestParam("idEmpresa") Integer idEmpresa, // RECIBIR ID EMPRESA
                                   RedirectAttributes redirectAttributes) {

        if (usuarioDAO.findByEmail(email) != null) {
            redirectAttributes.addFlashAttribute("error", "El correo ya existe.");
            return "redirect:/registro";
        }

        try {
            Usuario nuevo = new Usuario();
            nuevo.setNombres(nombres);
            nuevo.setUsername(username);
            nuevo.setEmail(email);
            nuevo.setContrasena(password);
            nuevo.setTelefono(telefono);
            nuevo.setEstado(1); 
            
            // ASIGNAR EMPRESA SELECCIONADA
            Empresa empresaSeleccionada = empresaDAO.findById(idEmpresa).orElse(null);
            if (empresaSeleccionada == null) throw new Exception("Empresa no válida");
            nuevo.setEmpresa(empresaSeleccionada);
            // ---------------------------

            TipoUsuario rolCliente = tipoUsuarioDAO.findByNombreRol("CLIENTE");
            if (rolCliente == null) {
                 redirectAttributes.addFlashAttribute("error", "Error: Rol CLIENTE no existe.");
                 return "redirect:/registro";
            }
            
            nuevo.setTipoUsuario(rolCliente);
            usuarioDAO.save(nuevo);
            
            redirectAttributes.addFlashAttribute("exito", "¡Registro exitoso en " + empresaSeleccionada.getNombre() + "! Inicia sesión.");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/registro";
        }
    }

    // --- LOGOUT ---
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/";
    }
}