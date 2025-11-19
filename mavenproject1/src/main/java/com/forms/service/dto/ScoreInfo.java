package com.forms.service.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * RF17: Informações de score de uma questão
 *
 * Contém:
 * - Contagem de respostas por alternativa
 * - Percentuais calculados
 * - Score final da questão
 *
 * @author gabriela
 */
public class ScoreInfo {

    private Integer questaoId;
    private String questaoTexto;
    private Integer totalRespostas;

    // Alternativa ID → Contagem de respostas
    private Map<Integer, Integer> contagemPorAlternativa;

    // Alternativa ID → Percentual (0.0 a 1.0)
    private Map<Integer, BigDecimal> percentualPorAlternativa;

    // Alternativa ID → Score (percentual × peso)
    private Map<Integer, BigDecimal> scorePorAlternativa;

    // Score médio da questão (média dos scores das alternativas ponderada pelas respostas)
    private BigDecimal scoreGeral;

    public ScoreInfo() {
        this.contagemPorAlternativa = new HashMap<>();
        this.percentualPorAlternativa = new HashMap<>();
        this.scorePorAlternativa = new HashMap<>();
        this.totalRespostas = 0;
        this.scoreGeral = BigDecimal.ZERO;
    }

    // Getters e Setters

    public Integer getQuestaoId() {
        return questaoId;
    }

    public void setQuestaoId(Integer questaoId) {
        this.questaoId = questaoId;
    }

    public String getQuestaoTexto() {
        return questaoTexto;
    }

    public void setQuestaoTexto(String questaoTexto) {
        this.questaoTexto = questaoTexto;
    }

    public Integer getTotalRespostas() {
        return totalRespostas;
    }

    public void setTotalRespostas(Integer totalRespostas) {
        this.totalRespostas = totalRespostas;
    }

    public Map<Integer, Integer> getContagemPorAlternativa() {
        return contagemPorAlternativa;
    }

    public void setContagemPorAlternativa(Map<Integer, Integer> contagemPorAlternativa) {
        this.contagemPorAlternativa = contagemPorAlternativa;
    }

    public Map<Integer, BigDecimal> getPercentualPorAlternativa() {
        return percentualPorAlternativa;
    }

    public void setPercentualPorAlternativa(Map<Integer, BigDecimal> percentualPorAlternativa) {
        this.percentualPorAlternativa = percentualPorAlternativa;
    }

    public Map<Integer, BigDecimal> getScorePorAlternativa() {
        return scorePorAlternativa;
    }

    public void setScorePorAlternativa(Map<Integer, BigDecimal> scorePorAlternativa) {
        this.scorePorAlternativa = scorePorAlternativa;
    }

    public BigDecimal getScoreGeral() {
        return scoreGeral;
    }

    public void setScoreGeral(BigDecimal scoreGeral) {
        this.scoreGeral = scoreGeral;
    }

    /**
     * Adiciona uma contagem para uma alternativa
     */
    public void adicionarContagem(Integer alternativaId, Integer contagem) {
        this.contagemPorAlternativa.put(alternativaId, contagem);
    }

    /**
     * Adiciona um percentual para uma alternativa
     */
    public void adicionarPercentual(Integer alternativaId, BigDecimal percentual) {
        this.percentualPorAlternativa.put(alternativaId, percentual);
    }

    /**
     * Adiciona um score para uma alternativa
     */
    public void adicionarScore(Integer alternativaId, BigDecimal score) {
        this.scorePorAlternativa.put(alternativaId, score);
    }
}
