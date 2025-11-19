package com.forms.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * RF05: Representa um Processo Avaliativo que agrupa múltiplos formulários
 * Exemplo: "Avaliação Institucional Semestre 2025.2"
 *
 * Um processo avaliativo contém:
 * - Múltiplos formulários (para alunos, professores, coordenadores)
 * - Período de vigência
 * - Cursos, disciplinas e turmas envolvidas
 *
 * @author gabriela
 */
@Entity
@Table(name = "processo_avaliativo")
public class ProcessoAvaliativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_processo_avaliativo")
    private Integer id;

    @NotBlank(message = "Nome do processo avaliativo é obrigatório")
    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "Data de início é obrigatória")
    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @NotNull(message = "Data de fim é obrigatória")
    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;

    /**
     * Formulários que fazem parte deste processo avaliativo
     */
    @OneToMany(mappedBy = "processoAvaliativo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Formulario> formularios;

    /**
     * Indica se o processo está ativo
     */
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    /**
     * Usuário que criou o processo (geralmente coordenador ou admin)
     */
    @ManyToOne
    @JoinColumn(name = "criador_id", referencedColumnName = "id", nullable = false)
    private Usuario criador;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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

    // Getters e Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public Set<Formulario> getFormularios() {
        return formularios;
    }

    public void setFormularios(Set<Formulario> formularios) {
        this.formularios = formularios;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Usuario getCriador() {
        return criador;
    }

    public void setCriador(Usuario criador) {
        this.criador = criador;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Verifica se o processo avaliativo está no período ativo
     */
    public boolean isNoPeriodo() {
        LocalDateTime agora = LocalDateTime.now();
        return ativo && agora.isAfter(dataInicio) && agora.isBefore(dataFim);
    }

    /**
     * Adiciona um formulário ao processo
     */
    public void adicionarFormulario(Formulario formulario) {
        if (this.formularios == null) {
            this.formularios = new java.util.HashSet<>();
        }
        this.formularios.add(formulario);
        formulario.setProcessoAvaliativo(this);
    }
}
