package curso.api.rest.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import curso.api.rest.ApplicationContextLoad;
import curso.api.rest.model.Usuario;
import curso.api.rest.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
@Component
public class JWTTokenAutenticacaoService {
	
	// Tempo de validade do token 2 dias
	private static final long EXPIRATION_TIME = 172800000;
	
	// Uma senha unica para compor a autenticacao
	private static final String SECRET = "SenhaExtremamenteSecreta";
	
	// Prefixo padrao de token
	private static final String TOKEN_PREFIX = "Bearer";
	
	// Identificacao do cabecalho da resposta, para identificar o token
	private static final String HEADER_STRING = "Authorization";
	
	// Gerando token de autenticacao e adicionando ao cabecalho e resposta HTTP
	public void addAuthentication(HttpServletResponse response, String username) throws IOException {
		
		// Montagem do token
		// Chama o gerador de token, adiciona o usuario, tempo de expiracao (Data atual + 2 dias),
		// algoritmo de geracao para a senha e compactacao de tudo.
		String JWT = Jwts.builder()
				.setSubject(username)
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(SignatureAlgorithm.HS512, SECRET).compact();
		
		// Junta token com o prefixo
		String token = TOKEN_PREFIX + " " + JWT; // Ex. Bearer 54555dsdf4d54545www4e5...
		
		//Adiciona no cabecalho http
		response.addHeader(HEADER_STRING, token); // Ex. Authorization Bearer 54555dsdf4d54545www4e5...
		
		// Escreve token como resposta Json no corpo HTTP
		response.getWriter().write("{\""+ HEADER_STRING +"\": \""+ token +"\"}");
	}
	
	// Retorna o usuario validado com token ou caso nao seja valido retorna null
	public Authentication getAuthentication(HttpServletRequest request) {
		
		// Pega o token enviado no cabecalho HTTP
		String token = request.getHeader(HEADER_STRING);
		
		if(token != null) {
			// Faz a validacao do token do usuario na requisicao
			// Passa a senha unica, retira o Bearer com o replace, 
			// descompacta tudo e retorna somente o usuario
			String user = Jwts.parser()
					.setSigningKey(SECRET)
					.parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
					.getBody().getSubject();
			if(user != null) {
				// ApplicationContextLoad criada em curso.api.rest. 
				// Essa classe pega tudo o que foi carregado no contexto de aplicacao.
				// Sem ela corre o risco de nao recuperar o usuario.
				Usuario usuario = ApplicationContextLoad.getApplicationContext()
						.getBean(UsuarioRepository.class).findUserByLogin(user);
				
				// Retornar o usuario logado
				if(usuario != null) {
					return new UsernamePasswordAuthenticationToken
							(usuario.getLogin(), usuario.getSenha(), usuario.getAuthorities());
				}
			}
		}
		return null; // Nao autorizado
	}
	
}