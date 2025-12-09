package lp.grupal.web.model.dao;
import java.util.List; // Importar List
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import lp.grupal.web.model.SolicitudServicio;

public interface ISolicitudServicioDAO extends JpaRepository<SolicitudServicio, Integer> {

    long countByEstado(String estado);

    @Query("SELECT COUNT(s) FROM SolicitudServicio s WHERE s.empresa.idempresa = :idEmpresa AND s.estado = :estado")
    long countByEmpresaAndEstado(@Param("idEmpresa") Integer idEmpresa, @Param("estado") String estado);

    // --- NUEVO MÉTODO PARA LISTAR ---
    @Query("SELECT s FROM SolicitudServicio s WHERE s.empresa.idempresa = :idEmpresa AND s.estado = :estado ORDER BY s.fechaSolicitud DESC")
    List<SolicitudServicio> findByEmpresaAndEstadoOrderByFechaDesc(@Param("idEmpresa") Integer idEmpresa, @Param("estado") String estado);
}