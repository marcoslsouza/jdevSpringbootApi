package curso.api.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EntityScan(basePackages = {"curso.api.rest.model"})
@ComponentScan(basePackages = {"curso.*"}) // Injecao de dependencia
@EnableJpaRepositories(basePackages = {"curso.api.rest.repository"})
@EnableTransactionManagement
@EnableWebMvc
@RestController
@EnableAutoConfiguration

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
	}
}
