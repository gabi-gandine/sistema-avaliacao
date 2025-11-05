package com.forms.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Representa uma resposta de um usuário a uma questão
 * Sempre vinculada a um usuário, mesmo em avaliações anônimas
 */
@Entity
@Table(name = "resposta")
public class Resposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "questaoId", referencedColumnName = "id", nullable = false)
    private Questao questao;

    /**
     * RF03: SEMPRE rastreia qual usuário respondeu
     * Mesmo em avaliações anônimas, o sistema sabe quem respondeu
     */
    @ManyToOne
    @JoinColumn(name = "usuarioId", referencedColumnName = "id", nullable = false)
    private Usuario usuario;

    /**
     * Para questões abertas (TipoQuestao.ABERTA)
     */
    @Column(name = "textoResposta", columnDefinition = "TEXT")
    private String textoResposta;

    /**
     * Para questões de múltipla escolha
     * Relacionamento Many-to-Many pois uma resposta pode ter várias opções selecionadas
     */
    @ManyToMany
    @JoinTable(
        name = "resposta_opcao",
        joinColumns = @JoinColumn(name = "respostaId"),
        inverseJoinColumns = @JoinColumn(name = "opcaoId")
    )
    private Set<OpcaoResposta> opcoesSelecionadas;

    @Column(name = "dataResposta", nullable = false)
    private LocalDateTime dataResposta;

    @Column(name = "dataUltimaEdicao")
    private LocalDateTime dataUltimaEdicao;

    /**
     * Opcional: Registrar IP para auditoria (RNF04)
     */
    @Column(name = "ipAddress", length = 45)
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        dataResposta = LocalDateTime.now();
        dataUltimaEdicao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dataUltimaEdicao = LocalDateTime.now();
    }

    // Getters e Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Questao getQuestao() {
        return questao;
    }

    public void setQuestao(Questao questao) {
        this.questao = questao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTextoResposta() {
        return textoResposta;
    }

    public void setTextoResposta(String textoResposta) {
        this.textoResposta = textoResposta;
    }

    public Set<OpcaoResposta> getOpcoesSelecionadas() {
        return opcoesSelecionadas;
    }

    public void setOpcoesSelecionadas(Set<OpcaoResposta> opcoesSelecionadas) {
        this.opcoesSelecionadas = opcoesSelecionadas;
    }

    public LocalDateTime getDataResposta() {
        return dataResposta;
    }

    public void setDataResposta(LocalDateTime dataResposta) {
        this.dataResposta = dataResposta;
    }

    public LocalDateTime getDataUltimaEdicao() {
        return dataUltimaEdicao;
    }

    public void setDataUltimaEdicao(LocalDateTime dataUltimaEdicao) {
        this.dataUltimaEdicao = dataUltimaEdicao;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
