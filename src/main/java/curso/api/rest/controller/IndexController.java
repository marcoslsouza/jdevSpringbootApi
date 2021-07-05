package curso.api.rest.controller;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
	@CacheEvict(value = "cacheFindByIdUsuario", allEntries = true) // Limpa o cache que não é utilizado
	@CachePut("cacheFindByIdUsuario") // Identifica que vai ter atualizacoes e coloca no cache
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
	@CacheEvict(value = "cacheListUsuario", allEntries = true) // Necessita de ativar no pom.xml e CursoSpringRestApiApplication.java
	@CachePut("cacheListUsuario")
	public ResponseEntity<List<Usuario>> usuario() throws InterruptedException {
		List<Usuario> lista = (List<Usuario>) usuarioRepository.findAll();
		
		// Segura o processamento em 6 segundos simulando um processo lento, para testar o cache
		Thread.sleep(6000);
		
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
		
		// Ajuste para salvar telefones.
		// Varre o List<Telefone> em Usuario e seta o usuario aos telefones do List<Telefone>.
		// Motivo nao e recebido pelo JSON a FK usuario_id na entidade telefone.
		// Caso perca a associacao por alteracao no For abaixo acrescente @ManyToOne(optional = false) 
		// na propriedade usuario da entidade Telefone, para nao cadastrar telefone sem usuario.
		for(int i = 0; i < usuario.getTelefones().size(); i++) {
			usuario.getTelefones().get(i).setUsuario(usuario);
		}
		
		usuario.setSenha(this.criptografaSenha(usuario));
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);
	}
	
	@PutMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> atualizar(@RequestBody Usuario usuario) {
		
		for(int i = 0; i < usuario.getTelefones().size(); i++) {
			usuario.getTelefones().get(i).setUsuario(usuario);
		}
		
		// Atualizar a senha
		Usuario userTemp = usuarioRepository.findUserByLogin(usuario.getLogin());
		if(!userTemp.getSenha().equals(usuario.getSenha())) {
			usuario.setSenha(this.criptografaSenha(usuario));
		}
		
		usuario.setSenha(this.criptografaSenha(usuario));
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
	
	// Cadastra telefones para usuarios
	@PostMapping(value = "/{id}/telefone", produces = "application/json")
	public ResponseEntity<Telefone> cadastrar(@PathVariable(value = "id") Long id, @RequestBody Telefone telefone) {
		
		// Localiza o usuario
		Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException());
		telefone.setUsuario(usuario);
		
		// Salva o telefone 
		Telefone telefoneSalvo = telefoneRepository.save(telefone);
		
		return new ResponseEntity<Telefone>(telefoneSalvo, HttpStatus.OK);
	}
	
	private String criptografaSenha(Usuario usuario) {
		return new BCryptPasswordEncoder().encode(usuario.getSenha());
	}
}
