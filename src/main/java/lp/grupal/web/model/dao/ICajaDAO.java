package lp.grupal.web.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import lp.grupal.web.model.Caja;
import java.time.LocalDateTime;
import java.util.List;

public interface ICajaDAO extends JpaRepository<Caja, Integer> {
    
    @Query("SELECT c FROM Caja c WHERE c.empresa.idempresa = :idEmpresa AND c.fechaMovimiento BETWEEN :inicio AND :fin ORDER BY c.fechaMovimiento DESC")
    List<Caja> buscarPorEmpresaYFecha(@Param("idEmpresa") Integer idEmpresa, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}