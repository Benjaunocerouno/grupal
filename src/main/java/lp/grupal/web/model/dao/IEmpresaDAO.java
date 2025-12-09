package lp.grupal.web.model.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import lp.grupal.web.model.Empresa;

public interface IEmpresaDAO extends JpaRepository<Empresa, Integer> {

}
