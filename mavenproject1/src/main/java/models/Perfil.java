package models;

/**
 *
 * @author gabriela
 */
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "perfil")
public class Perfil{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name="nome")
    private String nome;   
    
    @OneToOne(mappedBy = "perfil")
    private Usuario usuario;

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
    
}
