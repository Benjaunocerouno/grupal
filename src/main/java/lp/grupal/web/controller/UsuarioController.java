package lp.grupal.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lp.grupal.web.model.Usuario;
import lp.grupal.web.model.dao.IUsuarioDAO;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    @Autowired
    private IUsuarioDAO usuarioDAO;

    @GetMapping
    public String listarUsuarios(Model model, @RequestParam(value = "buscar", required = false) String buscar, HttpSession session) {
        
        // 1. SEGURIDAD
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (usuarioSesion == null) return "redirect:/login";

        String rol = usuarioSesion.getTipoUsuario().getNombreRol();
        
        // Permitimos acceso a ADMIN, VENDEDOR y ADMIN_VENDEDOR
        if (!rol.equals("ADMIN") && !rol.equals("VENDEDOR") && !rol.equals("ADMIN_VENDEDOR")) {
            return "redirect:/"; 
        }

        // 2. OBTENER EMPRESA DEL USUARIO ACTUAL
        Integer idEmpresa = usuarioSesion.getEmpresa().getIdempresa();

        // 3. LÓGICA DE BÚSQUEDA (Filtrada por Empresa)
        List<Usuario> listaClientes;
        
        if (buscar != null && !buscar.isEmpty()) {
            // Buscamos solo clientes de ESTA empresa
            listaClientes = usuarioDAO.buscarClientesPorEmpresa(idEmpresa, buscar);
        } else {
            // Listamos solo clientes de ESTA empresa
            listaClientes = usuarioDAO.listarClientesPorEmpresa(idEmpresa);
        }
        
        // 4. CLIENTE TOP
        Map<String, Object> topCliente = null;
        try {
            topCliente = usuarioDAO.obtenerClienteTop();
        } catch (Exception e) {
            // Manejo silencioso si no hay ventas aún
        }
        
        // 5. ENVIAR A VISTA
        model.addAttribute("clientes", listaClientes);
        model.addAttribute("topCliente", topCliente);
        model.addAttribute("palabraClave", buscar);

        return "empresa/usuarios"; 
    }

    // Método para Bloquear/Desbloquear
    @GetMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable("id") Integer id, RedirectAttributes flash) {
        Usuario user = usuarioDAO.findById(id).orElse(null);
        
        if (user != null) {
            // Usamos Integer para el estado (1 o 0)
            int estadoActual = (user.getEstado() == null) ? 1 : user.getEstado();
            int nuevoEstado = (estadoActual == 1) ? 0 : 1;
            
            user.setEstado(nuevoEstado);
            usuarioDAO.save(user);
            
            String msj = (nuevoEstado == 1) ? "Usuario desbloqueado." : "Usuario bloqueado.";
            flash.addFlashAttribute("exito", msj);
        } else {
            flash.addFlashAttribute("error", "Usuario no encontrado.");
        }
        
        return "redirect:/admin/usuarios";
    }
}