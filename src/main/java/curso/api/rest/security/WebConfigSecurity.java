package curso.api.rest.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import curso.api.rest.service.ImplementacaoUserDetailsService;

/*Mapeia URL, endereco, autoriza ou bloqueia acesso a URL*/
@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter {
	
	// Classe de service criada para recuperar dados do usuario.
	@Autowired
	private ImplementacaoUserDetailsService implementacaoUserDetailsService;
	
	// Configura as requisicoes HTTP
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// Ativando a protecao contra usuarios que nao estao validados por token
		http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
		
		// Ativando a permissao para o acesso a página inicial do sistema Ex: sistema.com.br/index*/
		.disable().authorizeRequests().antMatchers("/").permitAll()
		.antMatchers("/index").permitAll()
		
		// URL de logout - Redireciona após o usuario deslogar do sistema
		.anyRequest().authenticated().and().logout().logoutSuccessUrl("/index")
		
		// Mapeia URL de logout e invalida o usuario
		.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
		
		// Filtra requisicoes de login para autenticacao
		.and().addFilterBefore(new JWTLoginFilter("/login", authenticationManager()), 
				UsernamePasswordAuthenticationFilter.class)
		
		// Filtra demais requisicoes para verificar a presenca do TOKEN JWT no HEADER HTTP
		.addFilterBefore(new JWTApiAutenticacaoFilter(), UsernamePasswordAuthenticationFilter.class);
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		// Service que ira consultar o usuario no banco de dados
		auth.userDetailsService(implementacaoUserDetailsService)
		// Padrao de codificacao de senha
		.passwordEncoder(new BCryptPasswordEncoder());
	}
}
