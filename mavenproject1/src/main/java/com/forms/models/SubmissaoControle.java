package com.forms.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * RF03, RF13: Registra quem respondeu cada formulário, ainda que a resposta possa ser anônima
 *
 * IMPORTANTE: O sistema SEMPRE sabe quem respondeu (RF03)
 * Mas pode optar por não mostrar essa informação nos relatórios (RF11, RF14, RF19)
 *
 * Esta entidade controla:
 * - Quem (usuário) respondeu
 * - Qual formulário
 * - Quando iniciou e quando finalizou
 * - Se a submissão está completa
 * - IP para auditoria (RNF04)
 *
 * A submissão gera um RespostaAgrupador que contém as respostas reais.
 * Essa separação permite manter o anonimato quando necessário.
 *
 * CONTROLE DE RESPOSTA ÚNICA (RF13):
 * A constraint UNIQUE (formularioId, usuarioId) garante que um usuário
 * só pode ter UMA submissão por formulário.
 *
 * @author gabriela
 */
@Entity
@Table(
    name = "submissao_controle",
    uniqueConstraints = @UniqueConstraint(columnNames = {"id_formulario", "id_usuario"})
)
public class SubmissaoControle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_submissao_controle")
    private Integer id;

    /**
     * Formulário que está sendo respondido
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_formulario", referencedColumnName = "id_formulario", nullable = false)
    private Formulario formulario;

    /**
     * RF03: SEMPRE registra qual usuário respondeu
     * Mesmo em formulários anônimos!
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id", nullable = false)
    private Usuario usuario;

    /**
     * Turma do aluno no contexto desta submissão
     * Útil quando o formulário é aplicado a múltiplas turmas
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_turma", referencedColumnName = "id")
    private Turma turma;

    /**
     * Timestamp de quando o usuário iniciou a resposta
     */
    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    /**
     * Timestamp de quando o usuário finalizou/submeteu a resposta
     */
    @Column(name = "data_finalizacao")
    private LocalDateTime dataFinalizacao;

    /**
     * RF10: Indica se o usuário completou todas as questões obrigatórias
     */
    @Column(name = "completa", nullable = false)
    private Boolean completa = false;

    /**
     * RNF04: IP de onde foi feita a resposta (auditoria)
     * Armazena IPv4 (15 chars) ou IPv6 (45 chars)
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User-Agent do navegador (para auditoria e detecção de fraudes)
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * RespostaAgrupador associado a esta submissão
     * Contém todas as respostas do usuário
     */
    @OneToOne(mappedBy = "submissaoControle", cascade = CascadeType.ALL)
    private RespostaAgrupador respostaAgrupador;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (dataInicio == null) {
            dataInicio = LocalDateTime.now();
        }
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

    public Formulario getFormulario() {
        return formulario;
    }

    public void setFormulario(Formulario formulario) {
        this.formulario = formulario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(LocalDateTime dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public Boolean getCompleta() {
        return completa;
    }

    public void setCompleta(Boolean completa) {
        this.completa = completa;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public RespostaAgrupador getRespostaAgrupador() {
        return respostaAgrupador;
    }

    public void setRespostaAgrupador(RespostaAgrupador respostaAgrupador) {
        this.respostaAgrupador = respostaAgrupador;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Métodos úteis

    /**
     * RF13: Finaliza a submissão da resposta
     */
    public void finalizar() {
        this.completa = true;
        this.dataFinalizacao = LocalDateTime.now();
    }

    /**
     * Verifica se a submissão está finalizada
     */
    public boolean isFinalizada() {
        return completa && dataFinalizacao != null;
    }

    /**
     * RF13: Verifica se ainda é permitido editar a submissão
     * Baseado na configuração do formulário e se ainda está no período
     */
    public boolean podeEditar() {
        if (formulario == null) {
            return false;
        }

        // Formulários anônimos não podem ser editados
        if (formulario.getIsAnonimo()) {
            return false;
        }

        // Verifica se o formulário permite edição
        if (!formulario.getPermiteEdicao()) {
            return false;
        }

        // Verifica se ainda está no período do formulário
        if (!formulario.isNoPeriodo()) {
            return false;
        }

        return true;
    }

    /**
     * Calcula tempo gasto na submissão (em minutos)
     */
    public Long getTempoGastoMinutos() {
        if (dataInicio == null) {
            return null;
        }

        LocalDateTime fim = dataFinalizacao != null ? dataFinalizacao : LocalDateTime.now();
        return java.time.Duration.between(dataInicio, fim).toMinutes();
    }
}
