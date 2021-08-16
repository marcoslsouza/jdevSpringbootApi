package curso.api.rest.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.br.CPF;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UsuarioDTO implements Serializable {

	private static final long serialVersionUID = -494208682935365884L;
	
	private Long id;
	
	private String userLogin;
	private String userNome;
	
	@CPF(message = "O CPF é inválido!")
	private String userCpf;
	private List<Telefone> userTelefones;
	
	private List<Role> roles = new ArrayList<Role>();
	
	public UsuarioDTO(Usuario usuario) {
		this.id = usuario.getId();
		this.userLogin = usuario.getLogin();
		this.userNome = usuario.getNome();
		this.userCpf = usuario.getCpf();
		this.userTelefones = usuario.getTelefones();
	}
}
