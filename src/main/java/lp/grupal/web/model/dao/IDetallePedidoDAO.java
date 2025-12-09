package lp.grupal.web.model.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import lp.grupal.web.model.DetallePedido;

public interface IDetallePedidoDAO extends JpaRepository<DetallePedido, Integer> {
}