package com.forms.models;

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
    
    @ManyToOne
    @JoinColumn(name = "aluno", referencedColumnName = "id")
    private Usuario aluno;

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
    public Usuario getAluno() {
        return aluno;
    }

    /**
     * @param aluno the alunos to set
     */
    public void setAluno(Usuario aluno) {
        this.aluno = aluno;
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
