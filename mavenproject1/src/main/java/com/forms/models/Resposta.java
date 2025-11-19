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

    /**
     * Questão que está sendo respondida
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_questao", referencedColumnName = "id", nullable = false)
    private Questao questao;

    /**
     * RF14, RF19: RespostaAgrupador é a camada de indireção para anonimato
     * Respostas NÃO apontam diretamente para o usuário!
     * Apontam para o RespostaAgrupador, que aponta para SubmissaoControle,
     * que finalmente aponta para o usuário.
     *
     * Em formulários anônimos, relatórios NÃO seguem este link,
     * mantendo assim o anonimato.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_resposta_agrupador", referencedColumnName = "id_resposta_agrupador", nullable = false)
    private RespostaAgrupador respostaAgrupador;

    /**
     * Para questões abertas (TipoQuestao.ABERTA)
     */
    @Column(name = "textoResposta", columnDefinition = "TEXT")
    private String textoResposta;

    /**
     * Para questões de múltipla escolha
     * Relacionamento Many-to-Many pois uma resposta pode ter várias alternativas selecionadas
     * (questões do tipo MULTIPLA_ESCOLHA_MULTIPLA)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "resposta_alternativa_selecionada",
        joinColumns = @JoinColumn(name = "id_resposta"),
        inverseJoinColumns = @JoinColumn(name = "id_alternativa")
    )
    private Set<Alternativa> alternativasSelecionadas;

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

    public RespostaAgrupador getRespostaAgrupador() {
        return respostaAgrupador;
    }

    public void setRespostaAgrupador(RespostaAgrupador respostaAgrupador) {
        this.respostaAgrupador = respostaAgrupador;
    }

    public String getTextoResposta() {
        return textoResposta;
    }

    public void setTextoResposta(String textoResposta) {
        this.textoResposta = textoResposta;
    }

    public Set<Alternativa> getAlternativasSelecionadas() {
        return alternativasSelecionadas;
    }

    public void setAlternativasSelecionadas(Set<Alternativa> alternativasSelecionadas) {
        this.alternativasSelecionadas = alternativasSelecionadas;
    }

    /**
     * Adiciona uma alternativa selecionada
     */
    public void adicionarAlternativaSelecionada(Alternativa alternativa) {
        if (this.alternativasSelecionadas == null) {
            this.alternativasSelecionadas = new java.util.HashSet<>();
        }
        this.alternativasSelecionadas.add(alternativa);
    }

    /**
     * APENAS para auditoria: obtém o usuário que respondeu
     * NÃO usar em relatórios de formulários anônimos!
     */
    public Usuario getUsuarioParaAuditoria() {
        if (respostaAgrupador != null) {
            return respostaAgrupador.getUsuarioParaAuditoria();
        }
        return null;
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
