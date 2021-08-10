package curso.api.rest.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TelefoneDTO {

	private Long id;
	
	private String numero;
	
	private Usuario usuario;
	
	public TelefoneDTO(Telefone telefone) {
		
		this.id = telefone.getId();
		this.numero = telefone.getNumero();
		this.usuario = telefone.getUsuario();
	}
}
