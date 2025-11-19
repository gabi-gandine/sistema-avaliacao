package com.forms.service;

import com.forms.models.*;
import com.forms.repository.*;
import com.forms.service.dto.RelatorioFormulario;
import com.forms.service.dto.ScoreInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service para geração de Relatórios
 *
 * Implementa:
 * - RF16: Relatórios consolidados por curso, disciplina, turma e professor
 * - RF17: Cálculo de scores com base em pesos das alternativas
 * - RF19: Professores visualizam apenas resultados agregados (anonimato)
 *
 * ALGORITMO DE CÁLCULO DE SCORE (RF17):
 * Para cada alternativa:
 *   1. Conta quantas vezes foi selecionada
 *   2. Calcula percentual = (contagem / total de respostas)
 *   3. Score da alternativa = percentual × peso da alternativa
 * Score geral da questão = soma dos scores das alternativas
 *
 * ANONIMATO (RF19):
 * - Formulários anônimos: NÃO expõe identidade dos alunos
 * - Apenas dados agregados (contagens, percentuais, scores)
 * - Respostas abertas podem ser exibidas SEM identificação
 *
 * @author gabriela
 */
@Service
public class RelatorioService {

    @Autowired
    private FormularioRepository formularioRepository;

    @Autowired
    private RespostaAgrupadorRepository agrupadorRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private AlternativaRepository alternativaRepository;

    @Autowired
    private RespostaRepository respostaRepository;

    /**
     * RF16: Gera relatório de um formulário para uma turma específica
     *
     * @param formularioId ID do formulário
     * @param turmaId ID da turma (null = todas as turmas)
     * @return Relatório completo com scores
     */
    public RelatorioFormulario gerarRelatorioPorTurma(Integer formularioId, Integer turmaId) {
        Formulario formulario = formularioRepository.findById(formularioId)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        RelatorioFormulario relatorio = new RelatorioFormulario();
        relatorio.setFormularioId(formularioId);
        relatorio.setTitulo(formulario.getTitulo());
        relatorio.setDescricao(formulario.getDescricao());
        relatorio.setIsAnonimo(formulario.getIsAnonimo());
        relatorio.setNivel("TURMA");

        // RF19: Define se inclui identificação (apenas se não for anônimo)
        relatorio.setIncluiIdentificacao(!formulario.getIsAnonimo());

        // Busca respostas agrupadas
        List<RespostaAgrupador> agrupadores;
        if (turmaId != null) {
            agrupadores = agrupadorRepository.findRespostasFinalizadasPorTurmaEFormulario(turmaId, formularioId);
            relatorio.setFiltroAplicado("Turma ID: " + turmaId);
        } else {
            agrupadores = agrupadorRepository.findRespostasFinalizadasPorFormulario(formularioId);
            relatorio.setFiltroAplicado("Todas as turmas");
        }

        relatorio.setTotalSubmissoes(agrupadores.size());

        // Busca questões do formulário
        List<Questao> questoes = questaoRepository.findByFormularioIdOrderByOrdemAsc(formularioId);
        relatorio.setTotalQuestoes(questoes.size());

        // RF17: Calcula scores para cada questão
        BigDecimal somaScoresGerais = BigDecimal.ZERO;
        int questoesComScore = 0;

        for (Questao questao : questoes) {
            if (questao.getTipo() == TipoQuestao.ABERTA) {
                // Questões abertas: apenas coleta respostas (se não for anônimo)
                processarQuestaoAberta(questao, agrupadores, relatorio);
            } else {
                // Questões de múltipla escolha: calcula scores
                ScoreInfo score = calcularScoreQuestao(questao, agrupadores);
                relatorio.adicionarScore(questao.getId(), score);

                if (score.getScoreGeral().compareTo(BigDecimal.ZERO) > 0) {
                    somaScoresGerais = somaScoresGerais.add(score.getScoreGeral());
                    questoesComScore++;
                }
            }
        }

        // Calcula score médio geral do formulário
        if (questoesComScore > 0) {
            BigDecimal scoreGeral = somaScoresGerais.divide(
                    BigDecimal.valueOf(questoesComScore),
                    2,
                    RoundingMode.HALF_UP
            );
            relatorio.setScoreGeralFormulario(scoreGeral);
        }

        return relatorio;
    }

    /**
     * RF17: Calcula score de uma questão de múltipla escolha
     *
     * Algoritmo:
     * 1. Para cada alternativa da questão:
     *    - Conta quantas vezes foi selecionada
     *    - Calcula percentual
     *    - Multiplica pelo peso da alternativa
     * 2. Score geral = soma ponderada dos scores das alternativas
     */
    private ScoreInfo calcularScoreQuestao(Questao questao, List<RespostaAgrupador> agrupadores) {
        ScoreInfo scoreInfo = new ScoreInfo();
        scoreInfo.setQuestaoId(questao.getId());
        scoreInfo.setQuestaoTexto(questao.getTexto());

        // Busca todas as alternativas da questão
        List<Alternativa> alternativas = alternativaRepository.findByQuestaoId(questao.getId());

        // Conta total de respostas para esta questão
        int totalRespostas = 0;
        Map<Integer, Integer> contagemPorAlternativa = new HashMap<>();

        // Inicializa contagem com zero para todas as alternativas
        for (Alternativa alt : alternativas) {
            contagemPorAlternativa.put(alt.getId(), 0);
        }

        // Conta as respostas
        for (RespostaAgrupador agrupador : agrupadores) {
            Optional<Resposta> respostaOpt = respostaRepository
                    .findByRespostaAgrupadorAndQuestao(agrupador, questao);

            if (respostaOpt.isPresent()) {
                Resposta resposta = respostaOpt.get();
                Set<Alternativa> alternativasSelecionadas = resposta.getAlternativasSelecionadas();

                if (alternativasSelecionadas != null && !alternativasSelecionadas.isEmpty()) {
                    totalRespostas++;
                    for (Alternativa alt : alternativasSelecionadas) {
                        Integer contagem = contagemPorAlternativa.getOrDefault(alt.getId(), 0);
                        contagemPorAlternativa.put(alt.getId(), contagem + 1);
                    }
                }
            }
        }

        scoreInfo.setTotalRespostas(totalRespostas);

        // Calcula percentuais e scores
        BigDecimal somaScores = BigDecimal.ZERO;

        if (totalRespostas > 0) {
            for (Alternativa alt : alternativas) {
                Integer contagem = contagemPorAlternativa.get(alt.getId());
                scoreInfo.adicionarContagem(alt.getId(), contagem);

                // Calcula percentual (0.0 a 1.0)
                BigDecimal percentual = BigDecimal.valueOf(contagem)
                        .divide(BigDecimal.valueOf(totalRespostas), 4, RoundingMode.HALF_UP);
                scoreInfo.adicionarPercentual(alt.getId(), percentual);

                // RF17: Calcula score = percentual × peso
                if (alt.getPeso() != null) {
                    BigDecimal score = percentual.multiply(alt.getPeso());
                    scoreInfo.adicionarScore(alt.getId(), score);
                    somaScores = somaScores.add(score);
                }
            }
        }

        // Score geral da questão (soma dos scores das alternativas)
        scoreInfo.setScoreGeral(somaScores);

        return scoreInfo;
    }

    /**
     * Processa questão aberta (coleta respostas textuais)
     *
     * RF19: Se for anônimo, coleta respostas mas SEM identificação
     */
    private void processarQuestaoAberta(Questao questao, List<RespostaAgrupador> agrupadores,
                                       RelatorioFormulario relatorio) {
        List<String> respostasTextuais = new ArrayList<>();

        for (RespostaAgrupador agrupador : agrupadores) {
            Optional<Resposta> respostaOpt = respostaRepository
                    .findByRespostaAgrupadorAndQuestao(agrupador, questao);

            if (respostaOpt.isPresent()) {
                Resposta resposta = respostaOpt.get();
                String texto = resposta.getTextoResposta();

                if (texto != null && !texto.trim().isEmpty()) {
                    // RF19: Se for anônimo, NÃO incluir identificação
                    if (relatorio.getIsAnonimo()) {
                        // Apenas o texto da resposta, sem identificar quem respondeu
                        respostasTextuais.add(texto);
                    } else {
                        // Se não for anônimo, pode incluir identificação
                        Usuario usuario = agrupador.getUsuarioParaAuditoria();
                        if (usuario != null) {
                            respostasTextuais.add("[" + usuario.getNome() + "] " + texto);
                        } else {
                            respostasTextuais.add(texto);
                        }
                    }
                }
            }
        }

        if (!respostasTextuais.isEmpty()) {
            relatorio.adicionarRespostasAbertas(questao.getId(), respostasTextuais);
        }
    }

    /**
     * RF16: Gera relatório para um professor
     * Lista todas as turmas onde o professor leciona
     */
    public List<RelatorioFormulario> gerarRelatorioPorProfessor(Integer professorId, Integer formularioId) {
        // Busca formulário
        Formulario formulario = formularioRepository.findById(formularioId)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        // Busca todas as respostas agrupadas do formulário
        List<RespostaAgrupador> agrupadores = agrupadorRepository
                .findRespostasFinalizadasPorFormulario(formularioId);

        // Agrupa por turma
        Map<Integer, List<RespostaAgrupador>> agrupadoresPorTurma = agrupadores.stream()
                .filter(a -> a.getTurma() != null)
                .filter(a -> {
                    // Verifica se o professor leciona nesta turma
                    Set<Usuario> professores = a.getTurma().getProfessores();
                    return professores != null && professores.stream()
                            .anyMatch(p -> p.getId().equals(professorId));
                })
                .collect(Collectors.groupingBy(a -> a.getTurma().getId()));

        // Gera relatório para cada turma
        List<RelatorioFormulario> relatorios = new ArrayList<>();

        for (Map.Entry<Integer, List<RespostaAgrupador>> entry : agrupadoresPorTurma.entrySet()) {
            RelatorioFormulario relatorio = gerarRelatorioPorTurma(formularioId, entry.getKey());
            relatorios.add(relatorio);
        }

        return relatorios;
    }

    /**
     * RF16: Gera relatório consolidado geral (todos os dados)
     * Apenas para administradores/coordenadores
     */
    public RelatorioFormulario gerarRelatorioGeral(Integer formularioId) {
        return gerarRelatorioPorTurma(formularioId, null);
    }

    /**
     * Gera relatório simplificado (apenas estatísticas básicas)
     */
    public Map<String, Object> gerarEstatisticasBasicas(Integer formularioId) {
        Formulario formulario = formularioRepository.findById(formularioId)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        Map<String, Object> estatisticas = new HashMap<>();
        estatisticas.put("formularioId", formularioId);
        estatisticas.put("titulo", formulario.getTitulo());
        estatisticas.put("isAnonimo", formulario.getIsAnonimo());

        Long totalSubmissoes = agrupadorRepository.countRespostasFinalizadasPorFormulario(formularioId);
        estatisticas.put("totalSubmissoes", totalSubmissoes);

        Long totalQuestoes = questaoRepository.countByFormularioId(formularioId);
        estatisticas.put("totalQuestoes", totalQuestoes);

        return estatisticas;
    }
}
