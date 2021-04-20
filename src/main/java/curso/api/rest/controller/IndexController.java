package curso.api.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuario")
public class IndexController {

	// http://localhost:8080/usuario/?nome=Marcos
	@GetMapping(value = "/", produces = "application/json")
	public ResponseEntity<String> init(@RequestParam (value = "nome", defaultValue = "(usuário indefinido)", required = true) String nome) {
		return new ResponseEntity<>("Olá "+ nome +" REST Spring Boot", HttpStatus.OK);
	}
}
