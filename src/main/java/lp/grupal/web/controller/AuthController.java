package lp.grupal.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lp.grupal.web.model.Empresa;      
import lp.grupal.web.model.TipoDocumento; // IMPORTAR
import lp.grupal.web.model.TipoUsuario;
import lp.grupal.web.model.Usuario;
import lp.grupal.web.model.dao.IEmpresaDAO;
import lp.grupal.web.model.dao.ITipoDocumentoDAO; // IMPORTAR DAO
import lp.grupal.web.model.dao.ITipoUsuarioDAO;
import lp.grupal.web.model.dao.IUsuarioDAO;

@Controller
public class AuthController {

    @Autowired private IUsuarioDAO usuarioDAO;
    @Autowired private ITipoUsuarioDAO tipoUsuarioDAO;
    @Autowired private IEmpresaDAO empresaDAO;
    @Autowired private ITipoDocumentoDAO tipoDocumentoDAO; // 1. INYECTAR DAO DOCUMENTOS

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

    // --- REGISTRO (CORREGIDO) ---
    @GetMapping("/registro")
    public String registro(Model model) {
        // Enviamos empresas
        List<Empresa> empresas = empresaDAO.findAll();
        model.addAttribute("empresas", empresas);
        
        // 2. ENVIAR TIPOS DE DOCUMENTO (DNI, RUC, ETC.)
        model.addAttribute("tiposDoc", tipoDocumentoDAO.findByEstadoTrue());
        
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@RequestParam("nombres") String nombres,
                                   @RequestParam("username") String username,
                                   @RequestParam("email") String email,
                                   @RequestParam("password") String password,
                                   @RequestParam("telefono") String telefono,
                                   @RequestParam("idEmpresa") Integer idEmpresa,
                                   // 3. RECIBIR NUEVOS CAMPOS
                                   @RequestParam("idTipoDoc") Integer idTipoDoc,
                                   @RequestParam("numDoc") String numDoc,
                                   RedirectAttributes redirectAttributes) {

        if (usuarioDAO.findByEmail(email) != null) {
            redirectAttributes.addFlashAttribute("error", "El correo ya existe.");
            return "redirect:/registro";
        }
        
        // Validar documento duplicado (Opcional pero recomendado)
        if (usuarioDAO.findByNumeroDocumento(numDoc) != null) {
            redirectAttributes.addFlashAttribute("error", "El número de documento ya está registrado.");
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
            
            // 4. GUARDAR TIPO Y NÚMERO DE DOCUMENTO
            TipoDocumento td = tipoDocumentoDAO.findById(idTipoDoc).orElse(null);
            if(td == null) throw new Exception("Tipo de documento inválido");
            
            nuevo.setTipoDocumento(td);
            nuevo.setNumeroDocumento(numDoc);
            
            // Si es RUC, podrías guardar la Razón Social en "nombres" o pedir otro campo, 
            // por ahora usaremos 'nombres' para ambos casos o Razón Social como null.
            if(td.getAbreviatura().equals("RUC")) {
                nuevo.setRazonSocial(nombres); // Asumimos que en el registro puso la Razón Social en el campo nombres
            }

            // Asignar Empresa
            Empresa empresaSeleccionada = empresaDAO.findById(idEmpresa).orElse(null);
            if (empresaSeleccionada == null) throw new Exception("Empresa no válida");
            nuevo.setEmpresa(empresaSeleccionada);

            // Asignar Rol Cliente
            TipoUsuario rolCliente = tipoUsuarioDAO.findByNombreRol("CLIENTE");
            if (rolCliente == null) {
                 throw new Exception("Error: Rol CLIENTE no existe.");
            }
            nuevo.setTipoUsuario(rolCliente);
            
            usuarioDAO.save(nuevo);
            
            redirectAttributes.addFlashAttribute("exito", "¡Registro exitoso! Inicia sesión.");
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