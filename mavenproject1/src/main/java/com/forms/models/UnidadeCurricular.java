package com.forms.models;

/**
 *
 * @author gabriela
 */
import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "unidadeCurricular")
public class UnidadeCurricular{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name="nome")
    private String nome;
    
    @Column(name="tipo")
    private String tipo;
    
    @OneToMany(mappedBy = "uc", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Turma> turmas;    
    
    @ManyToOne
    @JoinColumn(name = "ucId", referencedColumnName = "id")
    private Curso curso;

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
     * @return the tipo
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * @param tipo the tipo to set
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * @return the turmas
     */
    public Set<Turma> getTurmas() {
        return turmas;
    }

    /**
     * @param turmas the turmas to set
     */
    public void setTurmas(Set<Turma> turmas) {
        this.turmas = turmas;
    }

    /**
     * @return the curso
     */
    public Curso getCurso() {
        return curso;
    }

    /**
     * @param curso the curso to set
     */
    public void setCurso(Curso curso) {
        this.curso = curso;
    }
}
