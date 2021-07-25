package curso.api.rest.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import curso.api.rest.model.Telefone;
import curso.api.rest.model.Usuario;
import curso.api.rest.model.UsuarioDTO;
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
	public ResponseEntity<UsuarioDTO> init(@PathVariable(value = "id") Long id) {
		
		try {
			Optional<Usuario> usuarios = usuarioRepository.findById(id);
			return new ResponseEntity<UsuarioDTO>(new UsuarioDTO(usuarios.get()), HttpStatus.OK);
		} catch(NoSuchElementException e) {
			new NoSuchElementException("Nenhum registro encontrado!" + e);
		}
		return null;
		
		/*return usuarioRepository.findById(id).map(usuario -> { 
			return ResponseEntity.ok().body(usuario);
		}).orElse(ResponseEntity.notFound().build());*/
	}
	
	@GetMapping(value = "/", produces = "application/json")
	@CacheEvict(value = "cacheListUsuario", allEntries = true) // Necessita de ativar no pom.xml e CursoSpringRestApiApplication.java
	@CachePut("cacheListUsuario")
	public ResponseEntity<List<UsuarioDTO>> usuario() throws InterruptedException {
		
		List<Usuario> lista = (List<Usuario>) usuarioRepository.findAll();
		
		List<UsuarioDTO> usuarios = new ArrayList<UsuarioDTO>();
		for(Usuario usuario : lista) {
			UsuarioDTO dto = new UsuarioDTO();
			dto.setId(usuario.getId());
			dto.setUserLogin(usuario.getLogin());
			dto.setUserNome(usuario.getNome());
			dto.setUserCpf(usuario.getCpf());
			dto.setUserTelefones(usuario.getTelefones());
			usuarios.add(dto);
		}
		
		// Segura o processamento em 6 segundos simulando um processo lento, para testar o cache
		// Thread.sleep(6000);
		
		return new ResponseEntity<List<UsuarioDTO>>(usuarios, HttpStatus.OK);
	}
	
	// application/pdf
	@GetMapping(value = "/{id}/relatoriopdf", produces = "application/json")
	public ResponseEntity<Usuario> relatorio(@PathVariable(value="id") Long id) {
		Optional<Usuario> usuario = usuarioRepository.findById(id);
		return new ResponseEntity<Usuario>(usuario.get(), HttpStatus.OK);
	}
	
	@PostMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> cadastrar(@RequestBody Usuario usuario) throws IOException {
		
		// Ajuste para salvar telefones.
		// Varre o List<Telefone> em Usuario e seta o usuario aos telefones do List<Telefone>.
		// Motivo nao e recebido pelo JSON a FK usuario_id na entidade telefone.
		// Caso perca a associacao por alteracao no For abaixo acrescente @ManyToOne(optional = false) 
		// na propriedade usuario da entidade Telefone, para nao cadastrar telefone sem usuario.
		for(int i = 0; i < usuario.getTelefones().size(); i++) {
			usuario.getTelefones().get(i).setUsuario(usuario);
		}
		
		// Cadastra endereco
		if(usuario.getCep() != null && !usuario.getCep().isEmpty()) {
			StringBuilder dados = this.retornaDadosEndereco(usuario.getCep());
			
			// Converte os dados do array para JSON (Gson lib do Google)
			Usuario usuarioAux = new Gson().fromJson(dados.toString(), Usuario.class);
			usuario.setCep(usuarioAux.getCep());
			usuario.setLogradouro(usuarioAux.getLogradouro());
			usuario.setComplemento(usuarioAux.getComplemento());
			usuario.setBairro(usuarioAux.getBairro());
			usuario.setLocalidade(usuarioAux.getLocalidade());
			usuario.setUf(usuarioAux.getUf());
		}
		
		usuario.setSenha(this.criptografaSenha(usuario));
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);
	}
	
	// Chamada a API ViaCEP
	private StringBuilder retornaDadosEndereco(String cep) throws IOException {
		URL url = new URL("https://viacep.com.br/ws/"+cep+"/json/");
		
		// Abre a conexao
		URLConnection connection = url.openConnection();
		
		// Recebe os dados
		InputStream is = connection.getInputStream();
		
		// Preparar a leitura
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		
		// Ler linha por linha e monta um array
		String dados = "";
		StringBuilder jsonCep = new StringBuilder();
		while((cep = br.readLine()) != null) {
			jsonCep.append(cep);
		}
		
		return jsonCep;
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
