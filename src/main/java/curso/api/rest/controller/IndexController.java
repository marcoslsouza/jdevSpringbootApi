package curso.api.rest.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
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
import curso.api.rest.model.UsuarioDTO;
import curso.api.rest.repository.TelefoneRepository;
import curso.api.rest.repository.UsuarioRepository;
import curso.api.rest.service.ImplementacaoUserDetailsService;

@RestController
@RequestMapping("/usuario")
public class IndexController {

	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private TelefoneRepository telefoneRepository;
	
	@Autowired
	private ImplementacaoUserDetailsService implementacaoUserDetailsService;
	
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
	public ResponseEntity<List<UsuarioDTO>> usuario() {
		
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
	
	@GetMapping(value = "/por-nome/{nome}", produces = "application/json")
	@CacheEvict(value = "cachePesquisaUsuarioPorNome", allEntries = true) // Necessita de ativar no pom.xml e CursoSpringRestApiApplication.java
	@CachePut("cachePesquisaUsuarioPorNome")
	public ResponseEntity<List<UsuarioDTO>> pesquisarUsuarioPorNome(@PathVariable("nome") String nome) {
		
		List<Usuario> lista = (List<Usuario>) usuarioRepository.findUserByName(nome);
		
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
		
		return new ResponseEntity<List<UsuarioDTO>>(usuarios, HttpStatus.OK);
	}
	
	// application/pdf
	@GetMapping(value = "/{id}/relatoriopdf", produces = "application/json")
	public ResponseEntity<Usuario> relatorio(@PathVariable(value="id") Long id) {
		Optional<Usuario> usuario = usuarioRepository.findById(id);
		return new ResponseEntity<Usuario>(usuario.get(), HttpStatus.OK);
	}
	
	@PostMapping(value = "/", produces = "application/json")
	public ResponseEntity<UsuarioDTO> cadastrar(@RequestBody @Valid UsuarioDTO usuarioDTO) {
		
		// Converte para Usuario
		Usuario entrada = new Usuario();
		entrada.setId(usuarioDTO.getId());
		entrada.setNome(usuarioDTO.getUserNome());
		entrada.setLogin(usuarioDTO.getUserLogin());
		entrada.setCpf(usuarioDTO.getUserCpf());
		
		// Verifica a existencia do login
		if(this.verificaExistenciaLogin(entrada)) {
			throw new ConstraintViolationException("Login já cadastrado!", null);
		}
		
		// Ajuste para salvar telefones.
		// Varre o List<Telefone> em Usuario e seta o usuario aos telefones do List<Telefone>.
		// Motivo nao e recebido pelo JSON a FK usuario_id na entidade telefone.
		// Caso perca a associacao por alteracao no For abaixo acrescente @ManyToOne(optional = false) 
		// na propriedade usuario da entidade Telefone, para nao cadastrar telefone sem usuario.
		/*for(int i = 0; i < usuario.getTelefones().size(); i++) {
			usuario.getTelefones().get(i).setUsuario(usuario);
		}*/
		
		// Cadastra endereco
		/*
		 * Salvar futuramente o CEP
		 * 
		 * if(usuario.getCep() != null && !usuario.getCep().isEmpty()) {
			StringBuilder dados = this.retornaDadosEndereco(usuario.getCep());
			
			// Converte os dados do array para JSON (Gson lib do Google)
			Usuario usuarioAux = new Gson().fromJson(dados.toString(), Usuario.class);
			usuario.setCep(usuarioAux.getCep());
			usuario.setLogradouro(usuarioAux.getLogradouro());
			usuario.setComplemento(usuarioAux.getComplemento());
			usuario.setBairro(usuarioAux.getBairro());
			usuario.setLocalidade(usuarioAux.getLocalidade());
			usuario.setUf(usuarioAux.getUf());
		}*/
		
		// Salva uma senha padrao
		entrada.setSenha(new BCryptPasswordEncoder().encode("445566"));
		
		Usuario usuarioSalvo = usuarioRepository.save(entrada);
		
		try {
			// Cria Role (Funcao)
			this.implementacaoUserDetailsService.criaRole(usuarioSalvo.getId());
		} catch(Exception e) {
			this.usuarioRepository.deleteById(usuarioSalvo.getId());
		}
		
		// Converte para UsuarioDTO
		UsuarioDTO saida = new UsuarioDTO();
		saida.setId(entrada.getId());
		saida.setUserNome(entrada.getNome());
		saida.setUserLogin(entrada.getLogin());
		saida.setUserCpf(entrada.getCpf());
		
		return new ResponseEntity<UsuarioDTO>(saida, HttpStatus.OK);
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
	public ResponseEntity<UsuarioDTO> atualizar(@RequestBody @Valid UsuarioDTO usuarioDTO) {
		
		// Recebe UsuarioDTO e passa para Usuario
		Usuario entrada = new Usuario();
		entrada.setId(usuarioDTO.getId());
		entrada.setNome(usuarioDTO.getUserNome());
		entrada.setLogin(usuarioDTO.getUserLogin());
		entrada.setCpf(usuarioDTO.getUserCpf());
		//entrada.setTelefones(usuarioDTO.getUserTelefones());
		
		for(int i = 0; i < usuarioDTO.getUserTelefones().size(); i++) {
			usuarioDTO.getUserTelefones().get(i).setUsuario(entrada);
		}
		
		entrada.setTelefones(usuarioDTO.getUserTelefones());
		
		// Recupera a senha para salva-la novamente.
		Optional<Usuario> recuperaSenha = usuarioRepository.findById(entrada.getId());
		String senhaCriptografada = "";
		if(recuperaSenha.get().getSenha().length() <= 6) {
			senhaCriptografada = this.criptografaSenha(recuperaSenha.get());
			entrada.setSenha(senhaCriptografada);
		} else
			entrada.setSenha(recuperaSenha.get().getSenha());
		
		Usuario usuarioSalvo = usuarioRepository.save(entrada);
			
		// Retorna o UsuarioDTO para Usuario
		UsuarioDTO saida = new UsuarioDTO();
		saida.setId(usuarioSalvo.getId());
		saida.setUserNome(usuarioSalvo.getNome());
		saida.setUserLogin(usuarioSalvo.getLogin());
		saida.setUserCpf(usuarioSalvo.getCpf());
		saida.setUserTelefones(usuarioSalvo.getTelefones());
		
		return new ResponseEntity<UsuarioDTO>(saida, HttpStatus.OK);
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
	
	@DeleteMapping(value = "/remover-telefone/{id}", produces = "application/json")
	public ResponseEntity<?> excluirTelefone(@PathVariable("id") Long id) {
		
		return telefoneRepository.findById(id).map(tel -> {
			telefoneRepository.deleteById(id);
			return ResponseEntity.ok().build();
		}).orElse(ResponseEntity.notFound().build());
	}
	
	private String criptografaSenha(Usuario usuario) {
		return new BCryptPasswordEncoder().encode(usuario.getSenha());
	}
	
	private boolean verificaExistenciaLogin(Usuario usuario) {
		if(this.usuarioRepository.findUserByLogin(usuario.getLogin()) != null) {
			return true;
		} else {
			return false;
		}
	}
}
