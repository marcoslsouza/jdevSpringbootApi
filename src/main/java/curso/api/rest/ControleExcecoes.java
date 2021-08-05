package curso.api.rest;

import java.sql.SQLException;
import java.util.List;

import javax.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
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

	/*********************Tratamento de erros comuns*****************************************************/
	
	// handleExceptionInternal trata a maioria dos erros que podem acontecer
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
	
	/**********************************Tratamento de erros de Banco de dados ********************************/
	// Mapea alguns erros a nivel de banco de dados. 
	// (DataIntegrityViolationException => excluir registro com referencia em outros registros,)
	@ExceptionHandler({DataIntegrityViolationException.class, ConstraintViolationException.class,
		SQLException.class})
	protected ResponseEntity<Object> handleExceptionDataIntegry(Exception ex) {
		
		return new ResponseEntity<>(this.tratamentoDeErrosDoBancoDeDados(ex), HttpStatus.BAD_REQUEST);
	}
	
	private ObjetoErro tratamentoDeErrosDoBancoDeDados(Exception ex) {
			
		ObjetoErro objetoErro = new ObjetoErro();
		objetoErro.setError(this.tratamentoDeMsgDeErrosDoBancoDeDados(ex));
		// Sempre erros de back-end HttpStatus.INTERNAL_SERVER_ERROR
		objetoErro.setCode(HttpStatus.BAD_REQUEST + " ==> " + 
				HttpStatus.BAD_REQUEST.getReasonPhrase());
		
		return objetoErro;
	}
	
	private String tratamentoDeMsgDeErrosDoBancoDeDados(Exception ex) {
		String msg = "";
			
		/*if(ex instanceof DataIntegrityViolationException) {
			msg = ((DataIntegrityViolationException) ex).getCause().getCause().getMessage();
		} else
			if(ex instanceof ConstraintViolationException) {
				msg = ((ConstraintViolationException) ex).getCause().getCause().getMessage();
			} else
				if(ex instanceof SQLException) {
					msg = ((SQLException) ex).getCause().getCause().getMessage();
				} else {
					msg = ex.getMessage();
				  }
		
		return msg;*/
		
		return ex.getMessage();
	}
}
