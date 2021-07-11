package curso.api.rest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ObjetoErro {

	// Mensagem de erro
	private String error;
	
	// Codigo do erro
	private String code;
}
