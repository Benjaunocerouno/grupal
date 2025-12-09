package lp.grupal.web.model.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import lp.grupal.web.model.TipoDocumento;

public interface ITipoDocumentoDAO extends JpaRepository<TipoDocumento, Integer> {
    // Listar solo los activos para mostrarlos en el select
    List<TipoDocumento> findByEstadoTrue();
}