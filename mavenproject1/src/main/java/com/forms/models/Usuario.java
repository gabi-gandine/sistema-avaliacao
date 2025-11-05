package com.forms.models;

/**
 *
 * @author gabriela
 */
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "usuario",
       uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Usuario{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Column(name="nome", nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Column(name="email", nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Matrícula/SIAPE é obrigatória")
    @Column(name="matriculaSiape", nullable = false, length = 20)
    private String matriculaSiape;

    @Column(name="senhaHash", nullable = false)
    private String senhaHash;

    @NotNull(message = "Perfil é obrigatório")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idPerfil", referencedColumnName = "id", nullable = false)
    private Perfil perfil;

    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @OneToMany(mappedBy = "aluno")
    private Set<Turma> turmasComoAluno;

    @OneToMany(mappedBy = "professor")
    private Set<Turma> turmasComoProfessor;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
     * @return the matriculaSiape
     */
    public String getMatriculaSiape() {
        return matriculaSiape;
    }

    /**
     * @param matriculaSiape the matriculaSiape to set
     */
    public void setMatriculaSiape(String matriculaSiape) {
        this.matriculaSiape = matriculaSiape;
    }

    public Set<Turma> getTurmasComoAluno() {
        return turmasComoAluno;
    }

    public void setTurmasComoAluno(Set<Turma> turmasComoAluno) {
        this.turmasComoAluno = turmasComoAluno;
    }

    public Set<Turma> getTurmasComoProfessor() {
        return turmasComoProfessor;
    }

    public void setTurmasComoProfessor(Set<Turma> turmasComoProfessor) {
        this.turmasComoProfessor = turmasComoProfessor;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
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
