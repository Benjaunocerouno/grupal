package lp.grupal.web.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import lp.grupal.web.model.Proveedor;
import java.util.List;

public interface IProveedorDAO extends JpaRepository<Proveedor, Integer> {
    // Listar solo los activos
    List<Proveedor> findByActivoTrue();
}