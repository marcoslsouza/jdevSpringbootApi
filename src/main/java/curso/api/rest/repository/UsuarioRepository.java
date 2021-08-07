package curso.api.rest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import curso.api.rest.model.Usuario;

@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {
	
	//@Query(value = "SELECT * FROM usuario u WHERE u.login = ?1", nativeQuery = true)
	@Query("SELECT u FROM Usuario u WHERE u.login = ?1")
	Usuario findUserByLogin(String login);
	
	// @Modifying => porque é um update
	@Transactional
	@Modifying
	//@Query("UPDATE Usuario SET token = ?1 WHERE login = ?2")
	@Query(nativeQuery = true, value = "UPDATE usuario SET token = ?1 WHERE login = ?2")
	void atualizaTokkenDoUsuario(String tokken, String login);
	
	@Query("SELECT u FROM Usuario u WHERE u.nome LIKE %?1%")
	List<Usuario> findUserByName(String nome);
	
	@Modifying
	@Query(value = "INSERT INTO usuarios_role(usuario_id, role_id) VALUES(?1, (SELECT id FROM role WHERE nome_role = 'USER_ROLE'))", nativeQuery = true)
	void insereAcessoRolePadrao(Long idUser);
}
