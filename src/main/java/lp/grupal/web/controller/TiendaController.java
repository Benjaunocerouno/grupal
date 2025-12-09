package lp.grupal.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession; // Importar HttpSession
import lp.grupal.web.model.Categoria;
import lp.grupal.web.model.Empresa;
import lp.grupal.web.model.Producto;
import lp.grupal.web.model.Usuario;
import lp.grupal.web.model.dao.ICategoriaDAO;
import lp.grupal.web.model.dao.IEmpresaDAO; // Necesario para el logo/nombre
import lp.grupal.web.model.dao.IProductoDAO;

@Controller
public class TiendaController {

    @Autowired private IProductoDAO productoDAO;
    @Autowired private ICategoriaDAO categoriaDAO;
    @Autowired private IEmpresaDAO empresaDAO;

    @GetMapping("/tienda")
    public String tienda(Model model, HttpSession session) {
        
        // 1. Obtener Usuario y definir Empresa
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        Integer idEmpresa = 1; // Por defecto Empresa 1

        if (usuario != null) {
            idEmpresa = usuario.getEmpresa().getIdempresa();
        } 
        // Lógica opcional para visitantes que eligieron contexto
        else if (session.getAttribute("idEmpresaVisualizar") != null) {
            idEmpresa = (Integer) session.getAttribute("idEmpresaVisualizar");
        }

        try {
            // 2. Cargar Datos de la Empresa (Para el Logo/Nombre en el Navbar)
            Empresa datosEmpresa = empresaDAO.findById(idEmpresa).orElse(new Empresa());
            if (datosEmpresa.getNombre() == null) datosEmpresa.setNombre("TechSolutions");

            // 3. Cargar Listas FILTRADAS por Empresa
            List<Categoria> categorias = categoriaDAO.findByEmpresa(idEmpresa);
            List<Producto> productos = productoDAO.findByEmpresa(idEmpresa);

            // 4. Enviar a la Vista
            model.addAttribute("listaCategorias", categorias);
            model.addAttribute("listaProductos", productos);
            model.addAttribute("empresa", datosEmpresa); // Importante para el HTML dinámico

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("listaCategorias", new ArrayList<>());
            model.addAttribute("listaProductos", new ArrayList<>());
            model.addAttribute("empresa", new Empresa());
        }

        return "tienda"; 
    }
}