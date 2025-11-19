package com.forms.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * RNF04: Registra logs de auditoria do sistema
 *
 * Registra todas as ações importantes:
 * - Login/Logout
 * - Criação/edição de formulários
 * - Submissão de respostas
 * - Acesso a relatórios
 * - Tentativas de acesso negado
 * - Alterações administrativas
 *
 * Informações registradas:
 * - Quem fez (usuário)
 * - O que fez (ação)
 * - Quando fez (timestamp)
 * - Onde (IP)
 * - Detalhes adicionais (JSON)
 *
 * @author gabriela
 */
@Entity
@Table(name = "log_auditoria", indexes = {
    @Index(name = "idx_log_usuario", columnList = "id_usuario"),
    @Index(name = "idx_log_acao", columnList = "acao"),
    @Index(name = "idx_log_timestamp", columnList = "timestamp")
})
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long id;

    /**
     * Usuário que executou a ação
     * Pode ser NULL para ações anônimas (ex: tentativa de login falha)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id")
    private Usuario usuario;

    /**
     * Ação executada
     * Exemplos: LOGIN, LOGOUT, CRIAR_FORMULARIO, SUBMETER_RESPOSTA, etc.
     */
    @Column(name = "acao", nullable = false, length = 100)
    private String acao;

    /**
     * Descrição detalhada da ação
     */
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    /**
     * Tipo/categoria da ação para facilitar filtros
     * Exemplos: AUTENTICACAO, FORMULARIO, RESPOSTA, RELATORIO, ADMIN
     */
    @Column(name = "tipo", length = 50)
    private String tipo;

    /**
     * IP de origem da ação
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User-Agent do navegador
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Timestamp de quando a ação ocorreu
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Detalhes adicionais em formato JSON
     * Permite armazenar informações específicas da ação
     * Exemplos:
     * - {"formularioId": 123, "titulo": "Avaliação Docente"}
     * - {"tentativasLogin": 3, "bloqueado": true}
     */
    @Column(name = "detalhes", columnDefinition = "TEXT")
    private String detalhes;

    /**
     * Resultado da ação: SUCESSO, FALHA, NEGADO, etc.
     */
    @Column(name = "resultado", length = 50)
    private String resultado;

    /**
     * Mensagem de erro, se houver
     */
    @Column(name = "mensagem_erro", columnDefinition = "TEXT")
    private String mensagemErro;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(String detalhes) {
        this.detalhes = detalhes;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public void setMensagemErro(String mensagemErro) {
        this.mensagemErro = mensagemErro;
    }

    // Métodos de conveniência

    /**
     * Cria log de sucesso
     */
    public static LogAuditoria sucesso(Usuario usuario, String acao, String descricao) {
        LogAuditoria log = new LogAuditoria();
        log.setUsuario(usuario);
        log.setAcao(acao);
        log.setDescricao(descricao);
        log.setResultado("SUCESSO");
        return log;
    }

    /**
     * Cria log de falha
     */
    public static LogAuditoria falha(Usuario usuario, String acao, String descricao, String erro) {
        LogAuditoria log = new LogAuditoria();
        log.setUsuario(usuario);
        log.setAcao(acao);
        log.setDescricao(descricao);
        log.setResultado("FALHA");
        log.setMensagemErro(erro);
        return log;
    }

    /**
     * Cria log de acesso negado
     */
    public static LogAuditoria acessoNegado(Usuario usuario, String acao, String descricao) {
        LogAuditoria log = new LogAuditoria();
        log.setUsuario(usuario);
        log.setAcao(acao);
        log.setDescricao(descricao);
        log.setResultado("NEGADO");
        return log;
    }
}
