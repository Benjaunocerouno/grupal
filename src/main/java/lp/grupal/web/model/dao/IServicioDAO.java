package lp.grupal.web.model.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import lp.grupal.web.model.Servicio;

public interface IServicioDAO extends JpaRepository<Servicio, Integer> {
    
    // --- MÉTODOS SEGUROS (FILTRADOS POR EMPRESA) ---

    // 1. Listar servicios de la empresa actual
    @Query("SELECT s FROM Servicio s WHERE s.empresa.idempresa = :idEmpresa AND s.activo = true")
    List<Servicio> findByEmpresa(@Param("idEmpresa") Integer idEmpresa);

    // 2. Buscar servicios dentro de la empresa actual
    @Query("SELECT s FROM Servicio s WHERE s.empresa.idempresa = :idEmpresa AND s.activo = true AND s.nombre LIKE %:criterio%")
    List<Servicio> buscarPorEmpresaYCriterio(@Param("idEmpresa") Integer idEmpresa, @Param("criterio") String criterio);
    
    // --- MÉTODOS PÚBLICOS / LEGACY ---

    List<Servicio> findByActivoTrue(); // Para la tienda pública

    // Búsqueda global
    @Query("SELECT s FROM Servicio s WHERE s.activo = true AND " +
           "(s.nombre LIKE %:criterio% OR s.descripcion LIKE %:criterio%)")
    List<Servicio> buscarPorCriterio(@Param("criterio") String criterio);
}