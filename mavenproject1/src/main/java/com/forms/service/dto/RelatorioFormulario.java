package com.forms.service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RF16, RF19: DTO para Relatório de Formulário
 *
 * Contém:
 * - Informações básicas do formulário
 * - Estatísticas gerais
 * - Scores por questão (RF17)
 * - Respostas abertas (se não for anônimo)
 * - Respeita anonimato (RF19)
 *
 * @author gabriela
 */
public class RelatorioFormulario {

    // Informações do formulário
    private Integer formularioId;
    private String titulo;
    private String descricao;
    private Boolean isAnonimo;

    // Estatísticas gerais
    private Integer totalSubmissoes;
    private Integer totalQuestoes;
    private LocalDateTime dataGeracao;

    // RF17: Scores por questão
    private Map<Integer, ScoreInfo> scoresPorQuestao;

    // Score médio geral do formulário
    private BigDecimal scoreGeralFormulario;

    // Respostas abertas (apenas se não for anônimo)
    // Questao ID → Lista de respostas textuais
    private Map<Integer, List<String>> respostasAbertas;

    // RF19: Se for anônimo, não incluir identificação de usuários
    private boolean incluiIdentificacao;

    // Filtros aplicados (para rastreabilidade)
    private String nivel; // CURSO, DISCIPLINA, TURMA, PROFESSOR
    private String filtroAplicado; // Ex: "Turma: Estrutura de Dados 2024.2"

    public RelatorioFormulario() {
        this.scoresPorQuestao = new HashMap<>();
        this.respostasAbertas = new HashMap<>();
        this.scoreGeralFormulario = BigDecimal.ZERO;
        this.dataGeracao = LocalDateTime.now();
    }

    // Getters e Setters

    public Integer getFormularioId() {
        return formularioId;
    }

    public void setFormularioId(Integer formularioId) {
        this.formularioId = formularioId;
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

    public Boolean getIsAnonimo() {
        return isAnonimo;
    }

    public void setIsAnonimo(Boolean isAnonimo) {
        this.isAnonimo = isAnonimo;
    }

    public Integer getTotalSubmissoes() {
        return totalSubmissoes;
    }

    public void setTotalSubmissoes(Integer totalSubmissoes) {
        this.totalSubmissoes = totalSubmissoes;
    }

    public Integer getTotalQuestoes() {
        return totalQuestoes;
    }

    public void setTotalQuestoes(Integer totalQuestoes) {
        this.totalQuestoes = totalQuestoes;
    }

    public LocalDateTime getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(LocalDateTime dataGeracao) {
        this.dataGeracao = dataGeracao;
    }

    public Map<Integer, ScoreInfo> getScoresPorQuestao() {
        return scoresPorQuestao;
    }

    public void setScoresPorQuestao(Map<Integer, ScoreInfo> scoresPorQuestao) {
        this.scoresPorQuestao = scoresPorQuestao;
    }

    public BigDecimal getScoreGeralFormulario() {
        return scoreGeralFormulario;
    }

    public void setScoreGeralFormulario(BigDecimal scoreGeralFormulario) {
        this.scoreGeralFormulario = scoreGeralFormulario;
    }

    public Map<Integer, List<String>> getRespostasAbertas() {
        return respostasAbertas;
    }

    public void setRespostasAbertas(Map<Integer, List<String>> respostasAbertas) {
        this.respostasAbertas = respostasAbertas;
    }

    public boolean isIncluiIdentificacao() {
        return incluiIdentificacao;
    }

    public void setIncluiIdentificacao(boolean incluiIdentificacao) {
        this.incluiIdentificacao = incluiIdentificacao;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getFiltroAplicado() {
        return filtroAplicado;
    }

    public void setFiltroAplicado(String filtroAplicado) {
        this.filtroAplicado = filtroAplicado;
    }

    /**
     * Adiciona um score para uma questão
     */
    public void adicionarScore(Integer questaoId, ScoreInfo score) {
        this.scoresPorQuestao.put(questaoId, score);
    }

    /**
     * Adiciona respostas abertas para uma questão
     */
    public void adicionarRespostasAbertas(Integer questaoId, List<String> respostas) {
        this.respostasAbertas.put(questaoId, respostas);
    }

    /**
     * Adiciona uma resposta aberta individual
     */
    public void adicionarRespostaAberta(Integer questaoId, String resposta) {
        if (!this.respostasAbertas.containsKey(questaoId)) {
            this.respostasAbertas.put(questaoId, new ArrayList<>());
        }
        this.respostasAbertas.get(questaoId).add(resposta);
    }
}
