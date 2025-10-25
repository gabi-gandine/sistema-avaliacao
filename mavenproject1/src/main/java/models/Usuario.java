package models;

/**
 *
 * @author gabriela
 */
import jakarta.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "usuairo")
public class Usuario{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name="nome")
    private String nome;
    
    @Column(name="email")
    private String email;
    
    @Column(name="matriculaSiape")
    private String matriculaSemestre;
    
    @Column(name="senhaHash")
    private String senhaHash;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idPerfil", referencedColumnName = "id")
    private Perfil perfil;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Turma> turma;

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
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the matriculaSemestre
     */
    public String getMatriculaSemestre() {
        return matriculaSemestre;
    }

    /**
     * @param matriculaSemestre the matriculaSemestre to set
     */
    public void setMatriculaSemestre(String matriculaSemestre) {
        this.matriculaSemestre = matriculaSemestre;
    }

    /**
     * @return the senhaHash
     */
    public String getSenhaHash() {
        return senhaHash;
    }

    /**
     * @param senhaHash the senhaHash to set
     */
    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    /**
     * @return the perfil
     */
    public Perfil getPerfil() {
        return perfil;
    }

    /**
     * @param perfil the perfil to set
     */
    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }
    
    
}
