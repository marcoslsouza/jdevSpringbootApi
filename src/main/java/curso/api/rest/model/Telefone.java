package curso.api.rest.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode
public class Telefone {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@EqualsAndHashCode.Include
	private Long id;
	
	@EqualsAndHashCode.Exclude
	private String numero;
	
	@JsonIgnore
	@JoinColumn(name = "usuario_id")
	@ManyToOne(optional = false) // optional = false Nao cadastra telefone sem usuario.
	@EqualsAndHashCode.Exclude
	private Usuario usuario;
}
