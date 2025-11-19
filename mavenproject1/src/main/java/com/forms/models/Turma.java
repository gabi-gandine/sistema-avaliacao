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

    /**
     * RF05: Turma pode ter múltiplos professores
     * Relacionamento many-to-many através de tabela turma_professor
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "turma_professor",
        joinColumns = @JoinColumn(name = "id_turma"),
        inverseJoinColumns = @JoinColumn(name = "id_usuario_professor")
    )
    private Set<Usuario> professores;
    
    
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
     * @return the professores
     */
    public Set<Usuario> getProfessores() {
        return professores;
    }

    /**
     * @param professores the professores to set
     */
    public void setProfessores(Set<Usuario> professores) {
        this.professores = professores;
    }

    /**
     * Adiciona um professor à turma
     */
    public void adicionarProfessor(Usuario professor) {
        if (this.professores == null) {
            this.professores = new java.util.HashSet<>();
        }
        this.professores.add(professor);
    }

    /**
     * Remove um professor da turma
     */
    public void removerProfessor(Usuario professor) {
        if (this.professores != null) {
            this.professores.remove(professor);
        }
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

    /**
     * Retorna nome descritivo da turma
     *
     * @return Nome da turma no formato "Nome UC - Ano.Semestre"
     */
    public String getNome() {
        StringBuilder nome = new StringBuilder();

        if (uc != null && uc.getNome() != null) {
            nome.append(uc.getNome());
        } else {
            nome.append("Turma");
        }

        nome.append(" - ");
        nome.append(ano);
        nome.append(".");
        nome.append(semestre);

        return nome.toString();
    }
}
