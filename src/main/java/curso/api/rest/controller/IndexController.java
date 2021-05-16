package curso.api.rest.controller;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import curso.api.rest.model.Usuario;
import curso.api.rest.repository.UsuarioRepository;

@RestController
@RequestMapping("/usuario")
public class IndexController {

	@Autowired
	private UsuarioRepository usuarioRepository;
	
	// Exemplo com parametros na URL
	// http://localhost:8080/usuario/?nome=Marcos
	/*@GetMapping(value = "/", produces = "application/json")
	public ResponseEntity<String> init(@RequestParam (value = "nome", defaultValue = "(usuário indefinido)", required = true) String nome) {
		return new ResponseEntity<>("Olá "+ nome +" REST Spring Boot", HttpStatus.OK);
	}*/
	
	// Retorna o JSON usuario
	// @PathVariable porque e na arquitetura REST
	@GetMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<Usuario> init(@PathVariable(value = "id") Long id) {
		
		/*List<Usuario> lista = new ArrayList<>();
		
		Usuario usuario = new Usuario();
		usuario.setId(50L);
		usuario.setLogin("marcos@gmail.com");
		usuario.setNome("Marcos");
		usuario.setSenha("123");
		lista.add(usuario);
		
		Usuario usuario2 = new Usuario();
		usuario2.setId(51L);
		usuario2.setLogin("maria@gmail.com");
		usuario2.setNome("Maria");
		usuario2.setSenha("122");
		lista.add(usuario2);*/
		
		try {
			Optional<Usuario> usuarios = usuarioRepository.findById(id);
			// return ResponseEntity.ok(usuario) 
			return new ResponseEntity<Usuario>(usuarios.get(), HttpStatus.OK);
		} catch(NoSuchElementException e) {
			new NoSuchElementException("Nenhum registro encontrado!" + e);
		}
		return null;
	}
	
	@GetMapping(value = "/", produces = "application/json")
	public ResponseEntity<List<Usuario>> usuario() {
		List<Usuario> lista = (List<Usuario>) usuarioRepository.findAll();
		return new ResponseEntity<List<Usuario>>(lista, HttpStatus.OK);
	}
	
	// application/pdf
	@GetMapping(value = "/{id}/relatoriopdf", produces = "application/json")
	public ResponseEntity<Usuario> relatorio(@PathVariable(value="id") Long id) {
		Optional<Usuario> usuario = usuarioRepository.findById(id);
		return new ResponseEntity<Usuario>(usuario.get(), HttpStatus.OK);
	}
	
	@PostMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> cadastrar(@RequestBody Usuario usuario) {
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);
	}
}
