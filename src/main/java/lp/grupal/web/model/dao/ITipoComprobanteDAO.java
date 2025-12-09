package lp.grupal.web.model.dao;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import lp.grupal.web.model.TipoComprobante;

public interface ITipoComprobanteDAO extends JpaRepository<TipoComprobante, Integer> {
    @Query("SELECT t FROM TipoComprobante t WHERE t.empresa.idempresa = :idEmpresa")
    List<TipoComprobante> findByEmpresa(@Param("idEmpresa") Integer idEmpresa);
    
}