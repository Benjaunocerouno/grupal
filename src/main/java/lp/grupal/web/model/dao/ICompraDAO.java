package lp.grupal.web.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import lp.grupal.web.model.Compra;

import java.time.LocalDateTime;
import java.util.List;

public interface ICompraDAO extends JpaRepository<Compra, Integer> {
    
    List<Compra> findAllByOrderByFechaCompraDesc();

    List<Compra> findAllByFechaCompraBetween(LocalDateTime inicio, LocalDateTime fin);
}