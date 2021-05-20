package curso.api.rest.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode
public class Usuario implements Serializable {
	
	private static final long serialVersionUID = 3710651919511446683L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@EqualsAndHashCode.Include
	private Long id;
	
	@EqualsAndHashCode.Exclude
	private String login;
	
	@EqualsAndHashCode.Exclude
	private String senha;
	
	@EqualsAndHashCode.Exclude
	private String nome;
	
	@OneToMany(mappedBy = "usuario", orphanRemoval = true, cascade = CascadeType.ALL)
	@EqualsAndHashCode.Exclude
	private List<Telefone> telefones = new ArrayList<Telefone>();
}
