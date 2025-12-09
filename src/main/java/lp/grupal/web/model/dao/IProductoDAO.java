package lp.grupal.web.model.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import lp.grupal.web.model.Producto;

public interface IProductoDAO extends JpaRepository<Producto, Integer> {
    
    // --- MÉTODOS SEGUROS (FILTRADOS POR EMPRESA) ---

    // 1. Listar inventario de la empresa actual
    @Query("SELECT p FROM Producto p WHERE p.empresa.idempresa = :idEmpresa AND p.activo = true")
    List<Producto> findByEmpresa(@Param("idEmpresa") Integer idEmpresa);
    
    // 2. Buscar producto dentro de la empresa actual
    @Query("SELECT p FROM Producto p WHERE p.empresa.idempresa = :idEmpresa AND p.activo = true AND " +
           "(p.nombre LIKE %:criterio% OR p.marca LIKE %:criterio% OR p.codigoBarras LIKE %:criterio%)")
    List<Producto> buscarPorEmpresaYCriterio(@Param("idEmpresa") Integer idEmpresa, @Param("criterio") String criterio);

    // 3. Alerta de Stock (NUEVO E IMPORTANTE): Solo avisa si es de TU empresa
    @Query("SELECT p FROM Producto p WHERE p.empresa.idempresa = :idEmpresa AND p.stock <= p.stockMinimo AND p.activo = true")
    List<Producto> encontrarStockBajoPorEmpresa(@Param("idEmpresa") Integer idEmpresa);

    // --- MÉTODOS PÚBLICOS (PARA LA TIENDA GLOBAL) ---
    // Estos se usan en el index.html para mostrar productos a los clientes
    
    List<Producto> findByActivoTrue();

    List<Producto> findTop8ByActivoTrueOrderByIdproductoDesc();
    
    // Este se puede mantener por si el ADMIN global quiere buscar en todo
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND " +
           "(p.nombre LIKE %:criterio% OR p.marca LIKE %:criterio% OR p.codigoBarras LIKE %:criterio%)")
    List<Producto> buscarPorCriterio(@Param("criterio") String criterio);
    
    // Este queda como legacy o para el ADMIN global
    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockMinimo AND p.activo = true")
    List<Producto> encontrarStockBajo();
}