package lp.grupal.web.model.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import lp.grupal.web.model.Pedido;

public interface IPedidoDAO extends JpaRepository<Pedido, Integer> {

    // Contar total (Ya lo tenías)
    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.empresa.idempresa = :idEmpresa")
    long countByEmpresa(@Param("idEmpresa") Integer idEmpresa);

    // Contar por estado (Ya lo tenías)
    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.empresa.idempresa = :idEmpresa AND p.estado = :estado")
    long countByEmpresaAndEstado(@Param("idEmpresa") Integer idEmpresa, @Param("estado") String estado);

    // --- NUEVO MÉTODO QUE TE FALTA ---
    // Este lista los pedidos, filtrando por empresa y estado, ordenados por fecha
    @Query("SELECT p FROM Pedido p WHERE p.empresa.idempresa = :idEmpresa AND p.estado = :estado ORDER BY p.fechaPedido DESC")
    List<Pedido> findByEmpresaAndEstadoOrderByFechaPedidoDesc(@Param("idEmpresa") Integer idEmpresa, @Param("estado") String estado);
}