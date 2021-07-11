package curso.api.rest;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@ControllerAdvice
public class ControleExcecoes extends ResponseEntityExceptionHandler {

	// Array que determina as excecoes que serao interceptadas
	@ExceptionHandler({Exception.class, RuntimeException.class, Throwable.class})
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		
		// Retorna um JSON
		return new ResponseEntity<>(this.retornaJSONDeErros(ex, status), headers, status);
	}
	
	private ObjetoErro retornaJSONDeErros(Exception ex, HttpStatus status) {
		
		ObjetoErro objetoErro = new ObjetoErro();
		objetoErro.setError(this.trataMensagemDeErros(ex));
		// Status e uma descricao
		objetoErro.setCode(status.value() + " ==> " + status.getReasonPhrase());
		
		return objetoErro;
	}
	
	private String trataMensagemDeErros(Exception ex) {
		
		String msg = "";
		
		// Erro de argumento invalido em metodo
		if(ex instanceof MethodArgumentNotValidException) {
			List<ObjectError> list = ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors();
			for(ObjectError objectError : list) {
				msg += objectError.getDefaultMessage() + "\n"; 
			}
		} else { // Demais erros
			msg = ex.getMessage();
		}
		
		return msg;
	}
}
