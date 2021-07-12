package curso.api.rest.model;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioDTO implements Serializable {

	private static final long serialVersionUID = -494208682935365884L;
	
	private String userLogin;
	private String userNome;
	private String userCpf;
	private List<Telefone> userTelefones;
	
	public UsuarioDTO(Usuario usuario) {
		this.userLogin = usuario.getLogin();
		this.userNome = usuario.getNome();
		this.userCpf = usuario.getCpf();
		this.userTelefones = usuario.getTelefones();
	}
}
