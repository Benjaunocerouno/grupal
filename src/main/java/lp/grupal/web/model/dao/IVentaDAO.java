package lp.grupal.web.model.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import lp.grupal.web.model.Venta;

public interface IVentaDAO extends JpaRepository<Venta, Integer> {

    // 1. Listar ventas SOLO de la empresa actual
    @Query("SELECT v FROM Venta v WHERE v.empresa.idempresa = :idEmpresa ORDER BY v.fechaventa DESC")
    List<Venta> findByEmpresa(@Param("idEmpresa") Integer idEmpresa);

    // 2. Contar ventas SOLO de la empresa actual
    @Query("SELECT COUNT(v) FROM Venta v WHERE v.empresa.idempresa = :idEmpresa")
    long countByEmpresa(@Param("idEmpresa") Integer idEmpresa);

    // 3. Obtener el último número de comprobante (CORREGIDO)
    @Query("SELECT MAX(v.num_comprobante) FROM Venta v WHERE v.tipoComprobante.nombre = :tipo AND v.serie_comprobante = :serie")
    String obtenerUltimoNumero(@Param("tipo") String tipo, @Param("serie") String serie);

    // 4. Reporte por fechas y empresa
    @Query("SELECT v FROM Venta v WHERE v.empresa.idempresa = :idEmpresa AND v.fechaventa BETWEEN :inicio AND :fin")
    List<Venta> buscarPorEmpresaYFecha(@Param("idEmpresa") Integer idEmpresa, 
                                       @Param("inicio") LocalDateTime inicio, 
                                       @Param("fin") LocalDateTime fin);

    // 5. Reporte agrupado por vendedor
    @Query(value = "SELECT u.nombres as vendedor, COUNT(v.idventa) as cantidad, SUM(v.monto_total) as total " +
                   "FROM venta v " +
                   "JOIN usuario u ON v.id_empleado = u.idusuario " +
                   "WHERE u.id_empresa = :idEmpresa AND v.fechaventa BETWEEN :inicio AND :fin " +
                   "GROUP BY u.idusuario", nativeQuery = true)
    List<Map<String, Object>> reporteVentasPorUsuario(@Param("idEmpresa") Integer idEmpresa, 
                                                      @Param("inicio") LocalDateTime inicio, 
                                                      @Param("fin") LocalDateTime fin);
    // 6. Reporte Productos Más Vendidos (Top 5)
    @Query(value = "SELECT p.nombre as producto, SUM(d.cantidad) as total_vendido, SUM(d.subtotal) as total_ingreso " +
                   "FROM detalle_venta d " +
                   "JOIN venta v ON d.idventa = v.idventa " +
                   "JOIN producto p ON d.idproducto = p.idproducto " +
                   "WHERE v.id_empresa = :idEmpresa AND v.fechaventa BETWEEN :inicio AND :fin " +
                   "GROUP BY p.idproducto " +
                   "ORDER BY total_vendido DESC LIMIT 5", nativeQuery = true)
    List<Map<String, Object>> productosMasVendidos(@Param("idEmpresa") Integer idEmpresa, 
                                                   @Param("inicio") LocalDateTime inicio, 
                                                   @Param("fin") LocalDateTime fin);
}