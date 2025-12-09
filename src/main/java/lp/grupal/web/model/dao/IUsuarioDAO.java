package lp.grupal.web.model.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import lp.grupal.web.model.Usuario;

public interface IUsuarioDAO extends JpaRepository<Usuario, Integer> {

    // --- LOGIN (Global, busca por email único) ---
    Usuario findByEmail(String email);
    Usuario findByEmailAndContrasena(String email, String contrasena);

    // --- GESTIÓN DE CLIENTES (Multi-Empresa) ---

    // 1. Buscar clientes por nombre DENTRO de la empresa
    // (Asumiendo que idtipousuario = 2 es CLIENTE)
    @Query("SELECT u FROM Usuario u WHERE u.tipoUsuario.idtipousuario = 2 " +
           "AND u.empresa.idempresa = :idEmpresa " +
           "AND u.nombres LIKE %:keyword%")
    List<Usuario> buscarClientesPorEmpresa(@Param("idEmpresa") Integer idEmpresa, @Param("keyword") String keyword);

    // 2. Listar todos los clientes de la empresa
    @Query("SELECT u FROM Usuario u WHERE u.tipoUsuario.idtipousuario = 2 AND u.empresa.idempresa = :idEmpresa")
    List<Usuario> listarClientesPorEmpresa(@Param("idEmpresa") Integer idEmpresa);

    // 3. Cliente Top (Procedimiento Almacenado)
    // NOTA: Si quieres que esto sea por empresa, tendrás que modificar el SP en la base de datos
    // para que reciba un parámetro (IN _id_empresa INT). Por ahora lo dejamos tal cual.
    @Query(value = "CALL sp_cliente_top_compras()", nativeQuery = true)
    Map<String, Object> obtenerClienteTop();

    // Spring Boot traduce automáticamente "findByNumeroDocumento" a la consulta SQL correcta
    Usuario findByNumeroDocumento(String numeroDocumento);
}