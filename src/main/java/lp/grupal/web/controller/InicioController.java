package lp.grupal.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import lp.grupal.web.model.Empresa;
import lp.grupal.web.model.Producto;
import lp.grupal.web.model.Servicio;
import lp.grupal.web.model.Usuario;
import lp.grupal.web.model.dao.IEmpresaDAO;
import lp.grupal.web.model.dao.IProductoDAO;
import lp.grupal.web.model.dao.IServicioDAO;

@Controller
public class InicioController {

    @Autowired private IProductoDAO productoDAO;
    @Autowired private IServicioDAO servicioDAO;
    @Autowired private IEmpresaDAO empresaDAO;

    @GetMapping({"/", "/index", "/home"}) 
    public String inicio(Model model, HttpSession session) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        // 1. SEGURIDAD: SI ES EMPLEADO, REDIRIGIR AL PANEL
        // Los empleados (Admin, Vendedor) no deben ver la tienda como clientes
        if (usuario != null) {
            String rol = usuario.getTipoUsuario().getNombreRol();
            if (rol.equals("ADMIN") || rol.equals("VENDEDOR") || 
                rol.equals("ADMIN_VENDEDOR") || rol.equals("CAJERO")) {
                return "redirect:/admin/dashboard";
            }
        }

        // 2. DETERMINAR QUÉ EMPRESA VISUALIZAR
        // Por defecto: Empresa 1 (Principal) para invitados
        Integer idEmpresaAVisualizar = 1; 

        // Si el usuario es Cliente, forzamos a ver SU empresa registrada
        if (usuario != null) {
            idEmpresaAVisualizar = usuario.getEmpresa().getIdempresa();
        } 
        // (Opcional) Si permites "visitar" tiendas sin loguearse usando EmpresaContextController
        else if (session.getAttribute("idEmpresaVisualizar") != null) {
            idEmpresaAVisualizar = (Integer) session.getAttribute("idEmpresaVisualizar");
        }

        // 3. CARGAR DATOS FILTRADOS POR LA EMPRESA SELECCIONADA
        try {
            // A. Datos de la Empresa (Para mostrar el Logo y Nombre correctos en el Navbar)
            Empresa datosEmpresa = empresaDAO.findById(idEmpresaAVisualizar).orElse(new Empresa());
            if (datosEmpresa.getNombre() == null) datosEmpresa.setNombre("TechSolutions"); // Fallback

            // B. Cargar productos SOLO de esa empresa
            // Nota: Asegúrate de tener el método findByEmpresa en IProductoDAO
            List<Producto> productos = productoDAO.findByEmpresa(idEmpresaAVisualizar);
            
            // C. Cargar servicios SOLO de esa empresa
            // Nota: Asegúrate de tener el método findByEmpresa en IServicioDAO
            List<Servicio> servicios = servicioDAO.findByEmpresa(idEmpresaAVisualizar);

            // 4. ENVIAR AL HTML
            model.addAttribute("listaProductos", productos);
            model.addAttribute("listaServicios", servicios);
            model.addAttribute("empresa", datosEmpresa); // Fundamental para el título y logo dinámicos

        } catch (Exception e) {
            System.out.println("Error cargando inicio: " + e.getMessage());
            // Enviar listas vacías para evitar error en el HTML
            model.addAttribute("listaProductos", new ArrayList<>());
            model.addAttribute("listaServicios", new ArrayList<>());
            model.addAttribute("empresa", new Empresa());
        }

        return "index";
    }
}