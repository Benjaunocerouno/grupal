package lp.grupal.web.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import lp.grupal.web.model.TipoUsuario;

@Repository
public interface ITipoUsuarioDAO extends JpaRepository<TipoUsuario, Integer> {
    TipoUsuario findByNombreRol(String nombreRol);
}