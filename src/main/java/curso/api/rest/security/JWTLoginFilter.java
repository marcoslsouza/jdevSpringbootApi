package curso.api.rest.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import curso.api.rest.model.Usuario;

// Estabelece o gerente de token 
public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {

	// Configurando o gerenciador de autenticacao
	protected JWTLoginFilter(String url, AuthenticationManager authenticationManager) {
		
		// Obriga a autenticar a URL
		super(new AntPathRequestMatcher(url));
		
		// Gerenciador de autenticacao
		setAuthenticationManager(authenticationManager);
	}

	// Retorna o usuario ao ler todo o fluxo de dados do token
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		
		// Pega o token para validar (Processa todo o JSON recebido para pegar o usuario e transformar em Usuario.class)
		Usuario user = new ObjectMapper().readValue(request.getInputStream(), Usuario.class);
		
		// Retorna o usuario login, senha e acessos
		return getAuthenticationManager()
				.authenticate(new UsernamePasswordAuthenticationToken(user.getLogin(), user.getPassword()));
	}

	// Passa o usuario e o response capturados nos metodos acima para a classe criada "JWTTokenAutenticacaoService"
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		
		// Chamada para o metodo "addAuthentication" da classe criada "JWTTokenAutenticacaoService" para
		// gerar o token, para retornar para o navegador
		new JWTTokenAutenticacaoService().addAuthentication(response, authResult.getName());
	}
}
