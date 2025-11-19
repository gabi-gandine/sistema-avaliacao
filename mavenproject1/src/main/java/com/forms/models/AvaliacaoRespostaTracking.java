package com.forms.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * LEGADO: Registra quem respondeu cada avaliação (modelo antigo)
 *
 * @deprecated Use {@link SubmissaoControle} no lugar.
 * Esta classe é mantida por compatibilidade com dados existentes.
 * Novos desenvolvimentos devem usar a entidade SubmissaoControle (que trabalha com Formulario).
 *
 * RF03: O sistema SEMPRE sabe quem respondeu,
 * mas pode optar por não mostrar essa informação nos relatórios
 * quando a avaliação for marcada como anônima.
 */
@Deprecated
@Entity
@Table(name = "avaliacaoRespostaTracking",
       uniqueConstraints = @UniqueConstraint(columnNames = {"avaliacaoId", "usuarioId"}))
public class AvaliacaoRespostaTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "avaliacaoId", referencedColumnName = "id", nullable = false)
    private Avaliacao avaliacao;

    /**
     * SEMPRE registra qual usuário respondeu
     */
    @ManyToOne
    @JoinColumn(name = "usuarioId", referencedColumnName = "id", nullable = false)
    private Usuario usuario;

    @Column(name = "dataInicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "dataFinalizacao")
    private LocalDateTime dataFinalizacao;

    /**
     * Indica se o usuário completou todas as questões obrigatórias
     */
    @Column(name = "completa", nullable = false)
    private Boolean completa = false;

    /**
     * IP de onde foi feita a resposta (auditoria - RNF04)
     */
    @Column(name = "ipAddress", length = 45)
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        if (dataInicio == null) {
            dataInicio = LocalDateTime.now();
        }
    }

    // Getters e Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Avaliacao getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Avaliacao avaliacao) {
        this.avaliacao = avaliacao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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

    /**
     * Finaliza a resposta da avaliação
     */
    public void finalizar() {
        this.completa = true;
        this.dataFinalizacao = LocalDateTime.now();
    }
}
