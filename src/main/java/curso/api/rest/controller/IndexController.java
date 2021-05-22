package curso.api.rest.controller;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import curso.api.rest.model.Telefone;
import curso.api.rest.model.Usuario;
import curso.api.rest.repository.TelefoneRepository;
import curso.api.rest.repository.UsuarioRepository;

@RestController
@RequestMapping("/usuario")
public class IndexController {

	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private TelefoneRepository telefoneRepository;
	
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
		
		/*try {
			Optional<Usuario> usuarios = usuarioRepository.findById(id);
			return new ResponseEntity<Usuario>(usuarios.get(), HttpStatus.OK);
		} catch(NoSuchElementException e) {
			new NoSuchElementException("Nenhum registro encontrado!" + e);
		}
		return null;*/
		
		return usuarioRepository.findById(id).map(usuario -> { 
			return ResponseEntity.ok().body(usuario);
		}).orElse(ResponseEntity.notFound().build());
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
	
	@PutMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> atualizar(@RequestBody Usuario usuario) {
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);
	}
	
	@DeleteMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<?> excluir(@PathVariable Long id) {
		return usuarioRepository.findById(id).map(usuario -> {
			usuarioRepository.deleteById(id);
			return ResponseEntity.ok().build();
		}).orElse(ResponseEntity.notFound().build());
	}
	
	// Telefones
	@PostMapping(value = "/{id}/telefone", produces = "application/json")
	public ResponseEntity<Telefone> cadastrar(@PathVariable(value = "id") Long id, @RequestBody Telefone telefone) {
		
		// Localiza o usuario
		Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException());
		telefone.setUsuario(usuario);
		
		// Salva o telefone 
		Telefone telefoneSalvo = telefoneRepository.save(telefone);
		
		return new ResponseEntity<Telefone>(telefoneSalvo, HttpStatus.OK);
	}
}
