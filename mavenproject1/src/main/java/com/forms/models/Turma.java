package com.forms.models;

import java.util.Set;

/**
 *
 * @author gabriela
 */
import jakarta.persistence.*;

@Entity
@Table(name = "turma")
public class Turma{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name="ano")
    private int ano;
    
    @Column(name="semestre")
    private int semestre;
    
    @ManyToMany
    private Set<Usuario> alunos;

    @ManyToOne
    @JoinColumn(name = "professor", referencedColumnName = "id")
    private Usuario professor;
    
    
    @ManyToOne
    @JoinColumn(name = "ucId", referencedColumnName = "id")
    private UnidadeCurricular uc;
    
    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the ano
     */
    public Integer getAno() {
        return ano;
    }

    /**
     * @param ano the ano to set
     */
    public void setAno(Integer ano) {
        this.ano = ano;
    }

    /**
     * @return the semestre
     */
    public Integer getSemestre() {
        return semestre;
    }

    /**
     * @param semestre the semestre to set
     */
    public void setSemestre(Integer semestre) {
        this.semestre = semestre;
    }

    /**
     * @return the aluno
     */
    public Set<Usuario> getAlunos() {
        return alunos;
    }

    /**
     * @param aluno the alunos to set
     */
    public void setAlunos(Set<Usuario> alunos) {
        this.alunos = alunos;
    }

    public void setAluno(Usuario aluno) {
        if (this.alunos == null) {
            this.alunos = new java.util.HashSet<>();
        }
        this.alunos.add(aluno);
    }

    /**
     * @return the professor
     */
    public Usuario getProfessore() {
        return getProfessor();
    }

    /**
     * @param professor the professores to set
     */
    public void setProfessor(Usuario professor) {
        this.professor = professor;
    }

    /**
     * @return the professor
     */
    public Usuario getProfessor() {
        return professor;
    }

    /**
     * @return the uc
     */
    public UnidadeCurricular getUc() {
        return uc;
    }

    /**
     * @param uc the uc to set
     */
    public void setUc(UnidadeCurricular uc) {
        this.uc = uc;
    }

    
}
