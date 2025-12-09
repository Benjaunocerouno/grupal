package lp.grupal.web.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import lp.grupal.web.model.Caja;
import lp.grupal.web.model.Usuario;
import lp.grupal.web.model.Venta;
import lp.grupal.web.model.dao.ICajaDAO;
import lp.grupal.web.model.dao.IVentaDAO;

@Controller
@RequestMapping("/admin/reportes") // La URL en el navegador seguirá siendo /admin/reportes
public class ReporteController {

    @Autowired private IVentaDAO ventaDAO;
    @Autowired private ICajaDAO cajaDAO;

    @GetMapping
    public String verReportes(Model model, 
                              @RequestParam(value = "inicio", required = false) String fInicio,
                              @RequestParam(value = "fin", required = false) String fFin,
                              HttpSession session) {
        
        // 1. SEGURIDAD: Validar sesión y rol
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        String rol = usuario.getTipoUsuario().getNombreRol();
        // Ajusta los roles según tu lógica (quién puede ver reportes)
        if (!rol.equals("ADMIN") && !rol.equals("ADMIN_VENDEDOR") && !rol.equals("CAJERO")) {
            return "redirect:/admin/dashboard";
        }

        Integer idEmpresa = usuario.getEmpresa().getIdempresa();

        // 2. FILTRO DE FECHAS
        LocalDateTime inicio = (fInicio == null || fInicio.isEmpty()) 
                ? LocalDate.now().withDayOfMonth(1).atStartOfDay() 
                : LocalDate.parse(fInicio).atStartOfDay();

        LocalDateTime fin = (fFin == null || fFin.isEmpty()) 
                ? LocalDateTime.now() 
                : LocalDate.parse(fFin).atTime(LocalTime.MAX);

        // 3. CARGAR DATOS
        // Usamos tus métodos DAO existentes
        List<Venta> ventas = ventaDAO.buscarPorEmpresaYFecha(idEmpresa, inicio, fin);
        
        // Nota: Asegúrate de que este método exista en tu IVentaDAO o usa uno equivalente
        // Si no tienes reporteVentasPorUsuario, puedes comentar esta línea y la del modelo abajo
        List<Map<String, Object>> ventasPorUsuario = ventaDAO.reporteVentasPorUsuario(idEmpresa, inicio, fin); 
        
        List<Caja> caja = cajaDAO.buscarPorEmpresaYFecha(idEmpresa, inicio, fin);
        
        // Si ya agregaste el método de 'productosMasVendidos' en IVentaDAO, descomenta esto:
        List<Map<String, Object>> topProductos = ventaDAO.productosMasVendidos(idEmpresa, inicio, fin);

        // 4. CÁLCULOS KPI
        double totalVentas = ventas.stream().mapToDouble(Venta::getMonto_total).sum();
        
        double balanceCaja = caja.stream()
            .mapToDouble(c -> c.getTipoMovimiento().equals("INGRESO") ? c.getMonto() : -c.getMonto())
            .sum();

        // 5. ENVIAR A LA VISTA
        model.addAttribute("ventas", ventas);
        model.addAttribute("ventasPorUsuario", ventasPorUsuario);
        model.addAttribute("caja", caja);
        model.addAttribute("topProductos", topProductos); // Asegúrate de tener esto en el HTML
        
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("balanceCaja", balanceCaja);
        
        model.addAttribute("fInicio", (fInicio != null) ? fInicio : inicio.toLocalDate().toString());
        model.addAttribute("fFin", (fFin != null) ? fFin : fin.toLocalDate().toString());
        model.addAttribute("rolUsuario", rol);

        // --- ESTA ES LA CLAVE ---
        // Le dice a Spring: "Busca el archivo en templates/empresa/reportes.html"
        return "empresa/reportes"; 
    }
}