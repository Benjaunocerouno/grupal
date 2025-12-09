package lp.grupal.web.model.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import lp.grupal.web.model.Categoria;

public interface ICategoriaDAO extends JpaRepository<Categoria, Integer> {
    
    List<Categoria> findByActivoTrue();

    // --- OPCIÓN B: USAR NATIVE QUERY ---
    // Esto funciona aunque en tu modelo Categoria solo tengas "Integer id_empresa"
    @Query(value = "SELECT * FROM categoria WHERE id_empresa = :idEmpresa AND activo = 1", nativeQuery = true)
    List<Categoria> findByEmpresa(@Param("idEmpresa") Integer idEmpresa);
}