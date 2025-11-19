package com.forms.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * RF07: Representa um formulário de avaliação vinculado a um processo avaliativo
 *
 * Um formulário:
 * - Pertence a um ProcessoAvaliativo
 * - Tem perfis destinatários (quem pode responder)
 * - Pode ser anônimo ou identificado (RF11)
 * - Contém questões de diferentes tipos (RF08, RF09, RF10)
 * - Pode permitir edição das respostas (RF13)
 *
 * Exemplo: "Formulário de Avaliação Docente - Alunos" destinado apenas para perfil ALUNO
 *
 * @author gabriela
 */
@Entity
@Table(name = "formulario")
public class Formulario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_formulario")
    private Integer id;

    @NotBlank(message = "Título do formulário é obrigatório")
    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    /**
     * Processo avaliativo ao qual este formulário pertence
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_processo_avaliativo", referencedColumnName = "id_processo_avaliativo")
    private ProcessoAvaliativo processoAvaliativo;

    /**
     * RF07: Perfis para os quais este formulário está destinado
     * Exemplo: Apenas ALUNO pode responder, ou apenas PROFESSOR, etc.
     * Se vazio, qualquer perfil pode responder
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "formulario_perfil_destino",
        joinColumns = @JoinColumn(name = "id_formulario"),
        inverseJoinColumns = @JoinColumn(name = "id_perfil")
    )
    private Set<Perfil> perfisDestino;

    /**
     * RF11: Se true, as respostas não aparecem vinculadas ao usuário nos relatórios
     * Porém o sistema SEMPRE rastreia quem respondeu via SubmissaoControle (RF03)
     */
    @Column(name = "is_anonimo", nullable = false)
    private Boolean isAnonimo = false;

    /**
     * RF13: Permite edição das respostas enquanto o formulário estiver aberto
     * Apenas para formulários não-anônimos
     */
    @Column(name = "permite_edicao", nullable = false)
    private Boolean permiteEdicao = true;

    /**
     * Data e hora em que o formulário fica disponível para resposta
     */
    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    /**
     * Data e hora em que o formulário deixa de aceitar respostas
     */
    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    /**
     * Usuário que criou este formulário (geralmente professor ou coordenador)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criador_id", referencedColumnName = "id", nullable = false)
    private Usuario criador;

    /**
     * Questões deste formulário
     */
    @OneToMany(mappedBy = "formulario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private Set<Questao> questoes;

    /**
     * Controle de submissões (quem já respondeu)
     */
    @OneToMany(mappedBy = "formulario", cascade = CascadeType.ALL)
    private Set<SubmissaoControle> submissoes;

    /**
     * Indica se o formulário está ativo (visível para os usuários)
     */
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

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

    public ProcessoAvaliativo getProcessoAvaliativo() {
        return processoAvaliativo;
    }

    public void setProcessoAvaliativo(ProcessoAvaliativo processoAvaliativo) {
        this.processoAvaliativo = processoAvaliativo;
    }

    public Set<Perfil> getPerfisDestino() {
        return perfisDestino;
    }

    public void setPerfisDestino(Set<Perfil> perfisDestino) {
        this.perfisDestino = perfisDestino;
    }

    public Boolean getIsAnonimo() {
        return isAnonimo;
    }

    public void setIsAnonimo(Boolean isAnonimo) {
        this.isAnonimo = isAnonimo;
    }

    public Boolean getPermiteEdicao() {
        return permiteEdicao;
    }

    public void setPermiteEdicao(Boolean permiteEdicao) {
        this.permiteEdicao = permiteEdicao;
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

    public Usuario getCriador() {
        return criador;
    }

    public void setCriador(Usuario criador) {
        this.criador = criador;
    }

    public Set<Questao> getQuestoes() {
        return questoes;
    }

    public void setQuestoes(Set<Questao> questoes) {
        this.questoes = questoes;
    }

    public Set<SubmissaoControle> getSubmissoes() {
        return submissoes;
    }

    public void setSubmissoes(Set<SubmissaoControle> submissoes) {
        this.submissoes = submissoes;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    /**
     * Alias para getAtivo() (compatibilidade com naming convention)
     */
    public boolean isAtivo() {
        return ativo != null && ativo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Métodos úteis

    /**
     * Verifica se o formulário está no período ativo para respostas
     */
    public boolean isNoPeriodo() {
        if (!ativo) {
            return false;
        }

        LocalDateTime agora = LocalDateTime.now();

        // Se não tem data de início/fim definidas, considera sempre ativo
        if (dataInicio == null && dataFim == null) {
            return true;
        }

        if (dataInicio != null && agora.isBefore(dataInicio)) {
            return false;
        }

        if (dataFim != null && agora.isAfter(dataFim)) {
            return false;
        }

        return true;
    }

    /**
     * Verifica se um perfil específico pode responder este formulário
     */
    public boolean perfilPodeResponder(Perfil perfil) {
        // Se não há restrição de perfis, qualquer um pode responder
        if (perfisDestino == null || perfisDestino.isEmpty()) {
            return true;
        }

        // Verifica se o perfil está na lista de destinatários
        return perfisDestino.contains(perfil);
    }

    /**
     * Adiciona uma questão ao formulário
     */
    public void adicionarQuestao(Questao questao) {
        if (this.questoes == null) {
            this.questoes = new java.util.HashSet<>();
        }
        this.questoes.add(questao);
        questao.setFormulario(this);
    }

    /**
     * Adiciona um perfil destinatário
     */
    public void adicionarPerfilDestino(Perfil perfil) {
        if (this.perfisDestino == null) {
            this.perfisDestino = new java.util.HashSet<>();
        }
        this.perfisDestino.add(perfil);
    }
}
