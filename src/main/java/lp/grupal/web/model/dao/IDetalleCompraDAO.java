package lp.grupal.web.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import lp.grupal.web.model.DetalleCompra;

public interface IDetalleCompraDAO extends JpaRepository<DetalleCompra, Integer> {
}