package com.forms.service;

import com.forms.models.*;
import com.forms.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service CRÍTICO para gerenciar Submissões de Respostas
 *
 * Implementa:
 * - RF03: Sempre registra quem respondeu
 * - RF10: Valida questões obrigatórias
 * - RF13: Resposta única + permite edição
 * - RF14: Preserva anonimato através do RespostaAgrupador
 *
 * FLUXO DE SUBMISSÃO:
 * 1. iniciarSubmissao() - Cria SubmissaoControle + RespostaAgrupador
 * 2. salvarResposta() - Salva cada resposta individual
 * 3. finalizarSubmissao() - Valida obrigatoriedade e marca como completo
 *
 * @author gabriela
 */
@Service
public class SubmissaoService {

    @Autowired
    private SubmissaoControleRepository submissaoRepository;

    @Autowired
    private RespostaAgrupadorRepository agrupadorRepository;

    @Autowired
    private RespostaRepository respostaRepository;

    @Autowired
    private FormularioRepository formularioRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private AlternativaRepository alternativaRepository;

    @Autowired
    private com.forms.repository.UsuarioRepository usuarioRepository;

    /**
     * RF13: Inicia uma submissão de formulário
     *
     * Cria:
     * - SubmissaoControle (rastreia quem está respondendo)
     * - RespostaAgrupador (camada de indireção para anonimato)
     *
     * @param formularioId ID do formulário
     * @param usuario Usuário que está respondendo
     * @param turma Turma do usuário (opcional)
     * @param request HttpServletRequest para capturar IP e User-Agent
     * @return RespostaAgrupador criado
     * @throws IllegalArgumentException se usuário já respondeu (RF13)
     */
    @Transactional
    public RespostaAgrupador iniciarSubmissao(Integer formularioId, Usuario usuario,
                                              Turma turma, HttpServletRequest request) {
        // Busca o formulário
        Formulario formulario = formularioRepository.findById(formularioId)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        // RF13: Verifica se já respondeu (resposta única)
        if (submissaoRepository.jaRespondeu(formularioId, usuario.getId())) {
            // Se já respondeu, verifica se pode editar
            Optional<SubmissaoControle> submissaoExistente =
                    submissaoRepository.findByFormularioIdAndUsuarioId(formularioId, usuario.getId());

            if (submissaoExistente.isPresent()) {
                SubmissaoControle submissao = submissaoExistente.get();

                // Se pode editar, retorna o agrupador existente
                if (submissao.podeEditar()) {
                    return submissao.getRespostaAgrupador();
                }

                // Se não pode editar, lança exceção
                throw new IllegalArgumentException("Você já respondeu este formulário e não é permitido editar");
            }
        }

        // Verifica se o formulário está ativo e no período
        if (!formulario.getAtivo()) {
            throw new IllegalArgumentException("Formulário não está ativo");
        }

        if (!formulario.isNoPeriodo()) {
            throw new IllegalArgumentException("Formulário fora do período de resposta");
        }

        // Verifica se o perfil pode responder (RF07)
        if (!formulario.perfilPodeResponder(usuario.getPerfil())) {
            throw new IllegalArgumentException("Seu perfil não tem permissão para responder este formulário");
        }

        // Cria SubmissaoControle (RF03 - sempre registra quem respondeu)
        SubmissaoControle submissao = new SubmissaoControle();
        submissao.setFormulario(formulario);
        submissao.setUsuario(usuario);
        submissao.setTurma(turma);
        submissao.setDataInicio(LocalDateTime.now());
        submissao.setCompleta(false);

        // Captura IP e User-Agent para auditoria (RNF04)
        if (request != null) {
            String ipAddress = getClientIpAddress(request);
            submissao.setIpAddress(ipAddress);
            submissao.setUserAgent(request.getHeader("User-Agent"));
        }

        submissao = submissaoRepository.save(submissao);

        // Cria RespostaAgrupador (RF14 - camada de indireção para anonimato)
        RespostaAgrupador agrupador = new RespostaAgrupador();
        agrupador.setSubmissaoControle(submissao);
        agrupador.setTurma(turma);

        agrupador = agrupadorRepository.save(agrupador);

        // Vincula o agrupador à submissão
        submissao.setRespostaAgrupador(agrupador);
        submissaoRepository.save(submissao);

        return agrupador;
    }

    /**
     * Salva uma resposta para uma questão aberta
     *
     * @param agrupador RespostaAgrupador da submissão em andamento
     * @param questaoId ID da questão
     * @param textoResposta Texto da resposta
     * @return Resposta salva
     */
    @Transactional
    public Resposta salvarRespostaAberta(RespostaAgrupador agrupador, Integer questaoId,
                                        String textoResposta) {
        Questao questao = questaoRepository.findById(questaoId)
                .orElseThrow(() -> new IllegalArgumentException("Questão não encontrada"));

        if (questao.getTipo() != TipoQuestao.ABERTA) {
            throw new IllegalArgumentException("Questão não é do tipo aberta");
        }

        // Verifica se já existe resposta para esta questão (permite sobrescrever durante edição)
        Optional<Resposta> respostaExistente = respostaRepository
                .findByRespostaAgrupadorAndQuestao(agrupador, questao);

        Resposta resposta;
        if (respostaExistente.isPresent()) {
            // Atualiza resposta existente
            resposta = respostaExistente.get();
            resposta.setTextoResposta(textoResposta);
            resposta.setDataUltimaEdicao(LocalDateTime.now());
        } else {
            // Cria nova resposta
            resposta = new Resposta();
            resposta.setQuestao(questao);
            resposta.setRespostaAgrupador(agrupador);
            resposta.setTextoResposta(textoResposta);
        }

        return respostaRepository.save(resposta);
    }

    /**
     * Salva uma resposta para uma questão de múltipla escolha
     *
     * @param agrupador RespostaAgrupador da submissão em andamento
     * @param questaoId ID da questão
     * @param alternativasIds IDs das alternativas selecionadas
     * @return Resposta salva
     */
    @Transactional
    public Resposta salvarRespostaMultiplaEscolha(RespostaAgrupador agrupador, Integer questaoId,
                                                  Set<Integer> alternativasIds) {
        Questao questao = questaoRepository.findById(questaoId)
                .orElseThrow(() -> new IllegalArgumentException("Questão não encontrada"));

        if (questao.getTipo() == TipoQuestao.ABERTA) {
            throw new IllegalArgumentException("Questão não é do tipo múltipla escolha");
        }

        // Valida múltipla escolha única vs múltipla
        if (questao.getTipo() == TipoQuestao.MULTIPLA_ESCOLHA_UNICA && alternativasIds.size() > 1) {
            throw new IllegalArgumentException("Esta questão permite apenas uma alternativa");
        }

        // Busca as alternativas
        Set<Alternativa> alternativas = new HashSet<>();
        for (Integer altId : alternativasIds) {
            Alternativa alt = alternativaRepository.findById(altId)
                    .orElseThrow(() -> new IllegalArgumentException("Alternativa não encontrada: " + altId));

            // Verifica se a alternativa pertence à questão
            if (!alt.getQuestao().getId().equals(questaoId)) {
                throw new IllegalArgumentException("Alternativa não pertence a esta questão");
            }

            alternativas.add(alt);
        }

        // Verifica se já existe resposta para esta questão
        Optional<Resposta> respostaExistente = respostaRepository
                .findByRespostaAgrupadorAndQuestao(agrupador, questao);

        Resposta resposta;
        if (respostaExistente.isPresent()) {
            // Atualiza resposta existente
            resposta = respostaExistente.get();
            resposta.setAlternativasSelecionadas(alternativas);
            resposta.setDataUltimaEdicao(LocalDateTime.now());
        } else {
            // Cria nova resposta
            resposta = new Resposta();
            resposta.setQuestao(questao);
            resposta.setRespostaAgrupador(agrupador);
            resposta.setAlternativasSelecionadas(alternativas);
        }

        return respostaRepository.save(resposta);
    }

    /**
     * RF10: Finaliza uma submissão
     *
     * Valida que todas as questões obrigatórias foram respondidas
     * Marca a submissão como completa
     *
     * @param agrupadorId ID do RespostaAgrupador
     * @throws IllegalArgumentException se questões obrigatórias não foram respondidas
     */
    @Transactional
    public void finalizarSubmissao(Integer agrupadorId) {
        RespostaAgrupador agrupador = agrupadorRepository.findById(agrupadorId)
                .orElseThrow(() -> new IllegalArgumentException("Agrupador de respostas não encontrado"));

        SubmissaoControle submissao = agrupador.getSubmissaoControle();
        Formulario formulario = submissao.getFormulario();

        // RF10: Valida questões obrigatórias
        List<Questao> questoesObrigatorias = questaoRepository
                .findQuestoesObrigatoriasPorFormulario(formulario.getId());

        List<String> questoesNaoRespondidas = new ArrayList<>();

        for (Questao questao : questoesObrigatorias) {
            Optional<Resposta> resposta = respostaRepository
                    .findByRespostaAgrupadorAndQuestao(agrupador, questao);

            if (resposta.isEmpty()) {
                questoesNaoRespondidas.add("Questão " + questao.getOrdem() + ": " + questao.getTexto());
            } else {
                // Verifica se a resposta não está vazia
                Resposta r = resposta.get();
                boolean respostaVazia = false;

                if (questao.getTipo() == TipoQuestao.ABERTA) {
                    respostaVazia = r.getTextoResposta() == null || r.getTextoResposta().trim().isEmpty();
                } else {
                    respostaVazia = r.getAlternativasSelecionadas() == null ||
                                   r.getAlternativasSelecionadas().isEmpty();
                }

                if (respostaVazia) {
                    questoesNaoRespondidas.add("Questão " + questao.getOrdem() + ": " + questao.getTexto());
                }
            }
        }

        if (!questoesNaoRespondidas.isEmpty()) {
            throw new IllegalArgumentException("As seguintes questões obrigatórias não foram respondidas: " +
                    String.join(", ", questoesNaoRespondidas));
        }

        // Marca como completa
        submissao.finalizar();
        submissaoRepository.save(submissao);

        // Finaliza o agrupador
        agrupador.finalizar();
        agrupadorRepository.save(agrupador);
    }

    /**
     * RF13: Verifica se uma submissão pode ser editada
     */
    public boolean podeEditar(Integer agrupadorId) {
        RespostaAgrupador agrupador = agrupadorRepository.findById(agrupadorId)
                .orElseThrow(() -> new IllegalArgumentException("Agrupador de respostas não encontrado"));

        return agrupador.getSubmissaoControle().podeEditar();
    }

    /**
     * Busca todas as respostas de um agrupador
     */
    public Set<Resposta> buscarRespostasDoAgrupador(Integer agrupadorId) {
        RespostaAgrupador agrupador = agrupadorRepository.findById(agrupadorId)
                .orElseThrow(() -> new IllegalArgumentException("Agrupador de respostas não encontrado"));

        return agrupador.getRespostas();
    }

    /**
     * Busca a submissão de um usuário para um formulário
     */
    public Optional<SubmissaoControle> buscarSubmissao(Integer formularioId, Integer usuarioId) {
        return submissaoRepository.findByFormularioIdAndUsuarioId(formularioId, usuarioId);
    }

    /**
     * Cancela uma submissão em andamento (deleta rascunho)
     */
    @Transactional
    public void cancelarSubmissao(Integer agrupadorId) {
        RespostaAgrupador agrupador = agrupadorRepository.findById(agrupadorId)
                .orElseThrow(() -> new IllegalArgumentException("Agrupador de respostas não encontrado"));

        if (agrupador.isFinalizado()) {
            throw new IllegalArgumentException("Não é possível cancelar uma submissão já finalizada");
        }

        // Deleta o agrupador (cascade deletará as respostas e a submissão)
        agrupadorRepository.delete(agrupador);
    }

    /**
     * Extrai o endereço IP real do cliente (considerando proxies)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    // ========== MÉTODOS ADICIONAIS PARA CONTROLLERS ==========

    /**
     * Sobrecarga: Salva resposta aberta com agrupadorId (em vez de objeto)
     */
    @Transactional
    public Resposta salvarRespostaAberta(Integer agrupadorId, Integer questaoId, String textoResposta) {
        RespostaAgrupador agrupador = buscarAgrupador(agrupadorId);
        return salvarRespostaAberta(agrupador, questaoId, textoResposta);
    }

    /**
     * Sobrecarga: Salva resposta múltipla escolha com agrupadorId (em vez de objeto)
     */
    @Transactional
    public Resposta salvarRespostaMultiplaEscolha(Integer agrupadorId, Integer questaoId, Set<Integer> alternativasIds) {
        RespostaAgrupador agrupador = buscarAgrupador(agrupadorId);
        return salvarRespostaMultiplaEscolha(agrupador, questaoId, alternativasIds);
    }

    /**
     * Busca um agrupador por ID
     */
    public RespostaAgrupador buscarAgrupador(Integer agrupadorId) {
        return agrupadorRepository.findById(agrupadorId)
                .orElseThrow(() -> new IllegalArgumentException("Agrupador de respostas não encontrado"));
    }

    /**
     * Alias para buscarRespostasDoAgrupador (compatibilidade)
     */
    public Set<Resposta> buscarRespostasPorAgrupador(Integer agrupadorId) {
        return buscarRespostasDoAgrupador(agrupadorId);
    }

    /**
     * Busca submissão do usuário e retorna RespostaAgrupador (compatibilidade)
     */
    public Optional<RespostaAgrupador> buscarSubmissaoDoUsuario(Integer formularioId, Integer usuarioId) {
        Optional<SubmissaoControle> submissao = buscarSubmissao(formularioId, usuarioId);
        return submissao.map(SubmissaoControle::getRespostaAgrupador);
    }

    /**
     * Busca uma resposta individual por ID
     */
    public Resposta buscarResposta(Integer respostaId) {
        return respostaRepository.findById(respostaId)
                .orElseThrow(() -> new IllegalArgumentException("Resposta não encontrada"));
    }

    /**
     * RF13: Edita uma resposta aberta existente
     */
    @Transactional
    public Resposta editarRespostaAberta(Integer respostaId, String textoResposta) {
        Resposta resposta = buscarResposta(respostaId);

        // Verifica se é questão aberta
        if (resposta.getQuestao().getTipo() != TipoQuestao.ABERTA) {
            throw new IllegalArgumentException("Esta resposta não é de questão aberta");
        }

        // Verifica se pode editar
        RespostaAgrupador agrupador = resposta.getRespostaAgrupador();
        if (agrupador.isFinalizado() && !agrupador.getSubmissaoControle().podeEditar()) {
            throw new IllegalArgumentException("Esta resposta não pode mais ser editada");
        }

        resposta.setTextoResposta(textoResposta);
        resposta.setDataUltimaEdicao(LocalDateTime.now());

        return respostaRepository.save(resposta);
    }

    /**
     * RF13: Edita uma resposta de múltipla escolha existente
     */
    @Transactional
    public Resposta editarRespostaMultiplaEscolha(Integer respostaId, Set<Integer> alternativasIds) {
        Resposta resposta = buscarResposta(respostaId);

        // Verifica se é questão de múltipla escolha
        if (resposta.getQuestao().getTipo() == TipoQuestao.ABERTA) {
            throw new IllegalArgumentException("Esta resposta não é de múltipla escolha");
        }

        // Verifica se pode editar
        RespostaAgrupador agrupador = resposta.getRespostaAgrupador();
        if (agrupador.isFinalizado() && !agrupador.getSubmissaoControle().podeEditar()) {
            throw new IllegalArgumentException("Esta resposta não pode mais ser editada");
        }

        // Valida e busca as alternativas
        Set<Alternativa> alternativas = new HashSet<>();
        for (Integer altId : alternativasIds) {
            Alternativa alternativa = alternativaRepository.findById(altId)
                    .orElseThrow(() -> new IllegalArgumentException("Alternativa não encontrada: " + altId));

            // Verifica se a alternativa pertence à questão
            if (!alternativa.getQuestao().getId().equals(resposta.getQuestao().getId())) {
                throw new IllegalArgumentException("Alternativa não pertence a esta questão");
            }

            alternativas.add(alternativa);
        }

        // Atualiza alternativas selecionadas
        resposta.getAlternativasSelecionadas().clear();
        resposta.getAlternativasSelecionadas().addAll(alternativas);
        resposta.setDataUltimaEdicao(LocalDateTime.now());

        return respostaRepository.save(resposta);
    }

    /**
     * RF13: Verifica se usuário já respondeu um formulário
     */
    public boolean jaRespondeu(Integer formularioId, Integer usuarioId) {
        return submissaoRepository.jaRespondeu(formularioId, usuarioId);
    }

    /**
     * Método simplificado para submeter respostas de formulário (para controllers MVC)
     * Aceita array de respostas e processa automaticamente
     */
    @Transactional
    public void submeterRespostas(Integer usuarioId, Integer formularioId, String[] respostas) {
        // TODO: Implementar lógica simplificada de submissão
        // Por enquanto, lançar exceção
        throw new UnsupportedOperationException("Método submeterRespostas simplificado ainda não implementado");
    }

    /**
     * Lista todas as submissões de um aluno específico
     */
    public Map<Formulario, Set<Resposta>> listarPorAluno(Integer alunoId) {
        // Buscar o usuário primeiro
        Usuario usuario = buscarUsuario(alunoId);

        List<SubmissaoControle> submissoes = submissaoRepository.findByUsuario(usuario);

        Map<Formulario, Set<Resposta>> respostasAgrupadas = new java.util.HashMap<>();

        for (SubmissaoControle submissao : submissoes) {
            if (Boolean.TRUE.equals(submissao.getCompleta())) {
                Formulario formulario = submissao.getFormulario();
                Set<Resposta> respostas = submissao.getRespostaAgrupador().getRespostas();
                respostasAgrupadas.put(formulario, respostas);
            }
        }

        return respostasAgrupadas;
    }

    /**
     * Helper method para buscar usuário por ID
     */
    private Usuario buscarUsuario(Integer usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }
}
