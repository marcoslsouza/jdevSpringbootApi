package curso.api.rest.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "role")
@Getter
@Setter

// Gera automaticamente uma sequencia
@SequenceGenerator(name = "seq_role", sequenceName = "seq_role", allocationSize = 1, initialValue = 1)
public class Role implements GrantedAuthority {
	
	private static final long serialVersionUID = -3936596085223178238L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_role") // seq_role sequencia criada acima
	private Long id;
	
	// Papel no sistema
	private String nomeRole;
	
	@ManyToMany(mappedBy = "roles")
	private List<Usuario> usuario;
	
	@ManyToMany
    @JoinTable(name = "roles_privileges", joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"), 
    inverseJoinColumns = @JoinColumn(name = "privilege_id", referencedColumnName = "id"))
    private List<Privilege> privileges; // Ex. Privilegio de leitura, escrita e etc. Um ou varios privilegios pertencem a uma role (Funcao)

	@Override
	// Retorna o nome no papel, acesso ou autorizacao. Ex. ROLE_GERENTE
	public String getAuthority() {
		
		return this.nomeRole;
	}
}
