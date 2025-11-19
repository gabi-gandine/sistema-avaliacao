package com.forms.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Representa uma alternativa (opção de resposta) para questões de múltipla escolha
 *
 * RF17: Cada alternativa possui um peso para cálculo de scores
 * Score = (percentual de respostas) × (peso da alternativa)
 *
 * Exemplo:
 * Questão: "Como avalia o professor?"
 * Alternativa 1: "Excelente" - peso 5.0
 * Alternativa 2: "Bom" - peso 4.0
 * Alternativa 3: "Regular" - peso 3.0
 *
 * Se 60% responderam "Excelente", score = 0.6 × 5.0 = 3.0
 *
 * @author gabriela
 */
@Entity
@Table(name = "alternativa")
public class Alternativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alternativa")
    private Integer id;

    @NotBlank(message = "Texto da alternativa é obrigatório")
    @Column(name = "texto_alternativa", nullable = false, columnDefinition = "TEXT")
    private String textoAlternativa;

    /**
     * Ordem de exibição da alternativa
     */
    @Column(name = "ordem", nullable = false)
    private Integer ordem;

    /**
     * RF17: Peso da alternativa para cálculo de score
     * Valores típicos: 1.0 a 5.0 (escala Likert)
     * Pode ser NULL para questões que não usam scores
     */
    @Column(name = "peso", precision = 10, scale = 2)
    private BigDecimal peso;

    /**
     * Indica se esta é a alternativa correta (para questões de prova/quiz)
     * Diferente de "peso": usado em avaliações objetivas com resposta certa/errada
     */
    @Column(name = "is_correta", nullable = false)
    private Boolean isCorreta = false;

    /**
     * Questão à qual esta alternativa pertence
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_questao", referencedColumnName = "id", nullable = false)
    private Questao questao;

    // Getters e Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTextoAlternativa() {
        return textoAlternativa;
    }

    public void setTextoAlternativa(String textoAlternativa) {
        this.textoAlternativa = textoAlternativa;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public BigDecimal getPeso() {
        return peso;
    }

    public void setPeso(BigDecimal peso) {
        this.peso = peso;
    }

    public Boolean getIsCorreta() {
        return isCorreta;
    }

    public void setIsCorreta(Boolean isCorreta) {
        this.isCorreta = isCorreta;
    }

    public Questao getQuestao() {
        return questao;
    }

    public void setQuestao(Questao questao) {
        this.questao = questao;
    }

    // Métodos úteis

    /**
     * Define o peso baseado em escala Likert 5 pontos
     * @param posicaoLikert 1 = pior, 5 = melhor
     */
    public void setPesoLikert5(int posicaoLikert) {
        if (posicaoLikert < 1 || posicaoLikert > 5) {
            throw new IllegalArgumentException("Posição Likert deve estar entre 1 e 5");
        }
        this.peso = BigDecimal.valueOf(posicaoLikert);
    }

    /**
     * Define o peso baseado em escala Likert 7 pontos
     * @param posicaoLikert 1 = pior, 7 = melhor
     */
    public void setPesoLikert7(int posicaoLikert) {
        if (posicaoLikert < 1 || posicaoLikert > 7) {
            throw new IllegalArgumentException("Posição Likert deve estar entre 1 e 7");
        }
        this.peso = BigDecimal.valueOf(posicaoLikert);
    }
}
