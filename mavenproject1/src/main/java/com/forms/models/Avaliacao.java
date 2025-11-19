package com.forms.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * LEGADO: Representa um formulário de avaliação (modelo antigo)
 *
 * @deprecated Use {@link Formulario} no lugar.
 * Esta classe é mantida por compatibilidade com dados existentes.
 * Novos desenvolvimentos devem usar a entidade Formulario.
 */
@Deprecated
@Entity
@Table(name = "avaliacao")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Título da avaliação é obrigatório")
    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "Data de início é obrigatória")
    @Column(name = "dataInicio", nullable = false)
    private LocalDateTime dataInicio;

    @NotNull(message = "Data de fim é obrigatória")
    @Column(name = "dataFim", nullable = false)
    private LocalDateTime dataFim;

    /**
     * Se true, as respostas não aparecem vinculadas ao usuário nos relatórios
     * Porém o sistema SEMPRE rastreia quem respondeu via AvaliacaoRespostaTracking
     */
    @Column(name = "anonima", nullable = false)
    private Boolean anonima = false;

    /**
     * Permite edição das respostas enquanto estiver aberta
     */
    @Column(name = "permiteEdicao", nullable = false)
    private Boolean permiteEdicao = true;

    @ManyToOne
    @JoinColumn(name = "turmaId", referencedColumnName = "id")
    private Turma turma;

    @ManyToOne
    @JoinColumn(name = "criadorId", referencedColumnName = "id", nullable = false)
    private Usuario criador;

    @OneToMany(mappedBy = "avaliacao", cascade = CascadeType.ALL)
    private Set<AvaliacaoRespostaTracking> respostasTracking;

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

    // Getters e Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
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

    public Boolean getAnonima() {
        return anonima;
    }

    public void setAnonima(Boolean anonima) {
        this.anonima = anonima;
    }

    public Boolean getPermiteEdicao() {
        return permiteEdicao;
    }

    public void setPermiteEdicao(Boolean permiteEdicao) {
        this.permiteEdicao = permiteEdicao;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }

    public Usuario getCriador() {
        return criador;
    }

    public void setCriador(Usuario criador) {
        this.criador = criador;
    }

    public Set<AvaliacaoRespostaTracking> getRespostasTracking() {
        return respostasTracking;
    }

    public void setRespostasTracking(Set<AvaliacaoRespostaTracking> respostasTracking) {
        this.respostasTracking = respostasTracking;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Verifica se a avaliação está ativa (dentro do período)
     */
    public boolean isAtiva() {
        LocalDateTime agora = LocalDateTime.now();
        return agora.isAfter(dataInicio) && agora.isBefore(dataFim);
    }
}
