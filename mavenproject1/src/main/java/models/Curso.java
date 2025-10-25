package models;

/**
 *
 * @author gabriela
 */
import jakarta.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "curso")
public class Curso{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name="nome")
    private String nome;
    
    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UnidadeCurricular> uc;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the nome
     */
    public String getNome() {
        return nome;
    }

    /**
     * @param nome the nome to set
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * @return the uc
     */
    public Set<UnidadeCurricular> getUc() {
        return uc;
    }

    /**
     * @param uc the uc to set
     */
    public void setUc(Set<UnidadeCurricular> uc) {
        this.uc = uc;
    }
        
}
