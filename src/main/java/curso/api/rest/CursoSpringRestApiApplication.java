package curso.api.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EntityScan(basePackages = {"curso.api.rest.model"})
@ComponentScan(basePackages = {"curso.*"}) // Injecao de dependencia
@EnableJpaRepositories(basePackages = {"curso.api.rest.repository"})
@EnableTransactionManagement
@EnableWebMvc
@RestController
@EnableAutoConfiguration
@EnableCaching

// public class CursoSpringRestApiApplication implements WebMvcConfigurer
// Implementa este metodo "Configuracao centralizada"
/*
 * @Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/usuario/").allowedMethods("GET").allowedOrigins("C:/Users/marco/Desktop/teste_cross_origin.html");
	}
 * 
 */
public class CursoSpringRestApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CursoSpringRestApiApplication.class, args);
		// Imprime no console a senha criptografada para testes, no Postman por exemplo
		System.out.println("Senha criptografada para teste: " + new BCryptPasswordEncoder().encode("445566"));
	}
}
