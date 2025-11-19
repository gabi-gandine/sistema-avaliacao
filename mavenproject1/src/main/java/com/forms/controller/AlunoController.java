package com.forms.controller;

import com.forms.models.*;
import com.forms.service.*;
import com.forms.service.annotation.Auditavel;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller para funcionalidades do ALUNO
 *
 * ⭐ CRÍTICO: Gerencia todo o fluxo de submissão de respostas
 *
 * Responsabilidades:
 * - RF03: Sistema rastreia quem respondeu (via SubmissaoControle)
 * - RF07: Listar formulários disponíveis para o perfil do aluno
 * - RF10: Validar questões obrigatórias antes de finalizar
 * - RF13: Garantir resposta única (não permite duplicatas)
 * - RF14: Preservar anonimato (usa RespostaAgrupador)
 * - RF15: Visualizar próprias respostas (se não for anônimo e permitir edição)
 *
 * FLUXO DE SUBMISSÃO:
 * 1. Aluno lista formulários disponíveis (GET /formularios-disponiveis)
 * 2. Aluno inicia submissão (POST /submissao/iniciar)
 *    → Cria SubmissaoControle (RF03 - rastreia quem respondeu)
 *    → Cria RespostaAgrupador (RF14 - camada de indireção para anonimato)
 * 3. Aluno responde questões (POST /submissao/{agrupadorId}/respostas)
 *    → Respostas vinculadas ao RespostaAgrupador, NÃO ao Usuario
 * 4. Aluno finaliza submissão (POST /submissao/{agrupadorId}/finalizar)
 *    → Valida questões obrigatórias (RF10)
 *    → Marca submissão como finalizada
 *
 * Acesso: Todos os perfis podem submeter respostas (ALUNO, PROFESSOR, etc.)
 *
 * @author gabriela
 */
@RestController
@RequestMapping("/api/aluno")
@PreAuthorize("isAuthenticated()")
public class AlunoController {

    @Autowired
    private FormularioService formularioService;

    @Autowired
    private SubmissaoService submissaoService;

    @Autowired
    private QuestaoService questaoService;

    @Autowired
    private TurmaService turmaService;

    @Autowired
    private LogAuditoriaService logService;

    /**
     * RF07: Listar formulários disponíveis para o perfil do aluno
     *
     * Retorna apenas formulários:
     * - Ativos
     * - Vinculados a processos no período
     * - Que o perfil do aluno pode responder
     */
    @GetMapping("/formularios-disponiveis")
    public ResponseEntity<List<Formulario>> listarFormulariosDisponiveis(Authentication authentication) {
        Usuario aluno = (Usuario) authentication.getPrincipal();

        List<Formulario> formularios = formularioService.listarDisponiveisParaPerfil(aluno.getPerfil());

        return ResponseEntity.ok(formularios);
    }

    /**
     * RF07: Buscar detalhes de um formulário específico
     */
    @GetMapping("/formularios/{id}")
    public ResponseEntity<Map<String, Object>> visualizarFormulario(
            @PathVariable Integer id,
            Authentication authentication) {

        Usuario aluno = (Usuario) authentication.getPrincipal();

        Formulario formulario = formularioService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        // Verifica se o aluno pode responder este formulário
        if (!formulario.perfilPodeResponder(aluno.getPerfil())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // RF13: Verifica se já respondeu
        boolean jaRespondeu = submissaoService.jaRespondeu(id, aluno.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("formulario", formulario);
        response.put("jaRespondeu", jaRespondeu);
        response.put("podeEditar", formulario.getPermiteEdicao() && !formulario.getIsAnonimo());

        return ResponseEntity.ok(response);
    }

    /**
     * RF03, RF13, RF14: Iniciar submissão de resposta
     *
     * IMPORTANTE:
     * - Cria SubmissaoControle (RF03 - SEMPRE rastreia quem respondeu)
     * - Cria RespostaAgrupador (RF14 - camada de indireção para anonimato)
     * - Verifica unicidade (RF13 - não permite duplicatas)
     */
    @PostMapping("/submissao/iniciar")
    @Auditavel(acao = "INICIAR_SUBMISSAO", tipo = "SUBMISSAO")
    public ResponseEntity<Map<String, Object>> iniciarSubmissao(
            @RequestBody Map<String, Object> requestData,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario aluno = (Usuario) authentication.getPrincipal();

        Integer formularioId = (Integer) requestData.get("formularioId");
        Integer turmaId = (Integer) requestData.get("turmaId");

        // RF13: Verifica se já respondeu
        if (submissaoService.jaRespondeu(formularioId, aluno.getId())) {
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Você já respondeu este formulário");
            error.put("codigo", "JA_RESPONDEU");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        // Busca a turma
        Turma turma = turmaService.buscarPorId(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));

        // RF03 + RF14: Inicia submissão
        RespostaAgrupador agrupador = submissaoService.iniciarSubmissao(
                formularioId,
                aluno,
                turma,
                request
        );

        // Busca questões para o frontend
        List<Questao> questoes = questaoService.listarPorFormulario(formularioId);

        Map<String, Object> response = new HashMap<>();
        response.put("agrupadorId", agrupador.getId());
        response.put("questoes", questoes);
        response.put("mensagem", "Submissão iniciada com sucesso");

        logService.registrarSucesso(
                aluno,
                "INICIAR_SUBMISSAO",
                "Submissão iniciada para Formulário (ID: " + formularioId + ")",
                "SUBMISSAO",
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * RF14: Salvar resposta de uma questão
     *
     * IMPORTANTE: Resposta é vinculada ao RespostaAgrupador, NÃO ao Usuario
     * Isso preserva o anonimato (RF14, RF19)
     */
    @PostMapping("/submissao/{agrupadorId}/respostas")
    @Auditavel(acao = "SALVAR_RESPOSTA", tipo = "SUBMISSAO")
    public ResponseEntity<Resposta> salvarResposta(
            @PathVariable Integer agrupadorId,
            @RequestBody Map<String, Object> requestData,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario aluno = (Usuario) authentication.getPrincipal();

        Integer questaoId = (Integer) requestData.get("questaoId");

        Questao questao = questaoService.buscarPorId(questaoId);

        Resposta resposta;

        if (questao.getTipo() == TipoQuestao.ABERTA) {
            // Questão aberta - salva texto
            String textoResposta = (String) requestData.get("textoResposta");
            resposta = submissaoService.salvarRespostaAberta(agrupadorId, questaoId, textoResposta);

        } else {
            // Questão de múltipla escolha - salva alternativas selecionadas
            @SuppressWarnings("unchecked")
            List<Integer> alternativasIds = (List<Integer>) requestData.get("alternativasIds");

            if (alternativasIds == null || alternativasIds.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("erro", "Nenhuma alternativa selecionada");
                return ResponseEntity.badRequest().body(null);
            }

            resposta = submissaoService.salvarRespostaMultiplaEscolha(
                    agrupadorId,
                    questaoId,
                    new HashSet<>(alternativasIds)
            );
        }

        return ResponseEntity.ok(resposta);
    }

    /**
     * RF10, RF13: Finalizar submissão
     *
     * IMPORTANTE:
     * - Valida todas as questões obrigatórias (RF10)
     * - Se faltar alguma, retorna erro com lista de questões faltantes
     * - Marca submissão como finalizada (não pode mais editar, exceto se RF12)
     */
    @PostMapping("/submissao/{agrupadorId}/finalizar")
    @Auditavel(acao = "FINALIZAR_SUBMISSAO", tipo = "SUBMISSAO")
    public ResponseEntity<Map<String, Object>> finalizarSubmissao(
            @PathVariable Integer agrupadorId,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario aluno = (Usuario) authentication.getPrincipal();

        try {
            // RF10: Valida questões obrigatórias
            submissaoService.finalizarSubmissao(agrupadorId);

            RespostaAgrupador agrupador = submissaoService.buscarAgrupador(agrupadorId);
            Formulario formulario = agrupador.getSubmissaoControle().getFormulario();

            logService.registrarSubmissaoResposta(
                    aluno,
                    formulario.getId(),
                    formulario.getTitulo(),
                    request
            );

            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Submissão finalizada com sucesso!");
            response.put("formularioTitulo", formulario.getTitulo());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // RF10: Questões obrigatórias não respondidas
            Map<String, Object> error = new HashMap<>();
            error.put("erro", e.getMessage());
            error.put("codigo", "QUESTOES_OBRIGATORIAS_FALTANTES");

            logService.registrarFalha(
                    aluno,
                    "FINALIZAR_SUBMISSAO",
                    "Tentativa de finalizar submissão sem responder questões obrigatórias",
                    "SUBMISSAO",
                    request,
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * RF15: Visualizar própria resposta
     *
     * Só é possível se:
     * - Formulário NÃO é anônimo
     * - Formulário permite edição (RF12)
     */
    @GetMapping("/minhas-respostas/formulario/{formularioId}")
    public ResponseEntity<Map<String, Object>> visualizarMinhasRespostas(
            @PathVariable Integer formularioId,
            Authentication authentication) {

        Usuario aluno = (Usuario) authentication.getPrincipal();

        Formulario formulario = formularioService.buscarPorId(formularioId)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        // RF19: Formulários anônimos não permitem visualização individual
        if (formulario.getIsAnonimo()) {
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Este formulário é anônimo. Não é possível visualizar respostas individuais.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Busca a submissão do aluno
        Optional<RespostaAgrupador> agrupadorOpt = submissaoService.buscarSubmissaoDoUsuario(
                formularioId,
                aluno.getId()
        );

        if (agrupadorOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Você ainda não respondeu este formulário");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        RespostaAgrupador agrupador = agrupadorOpt.get();

        // Busca todas as respostas
        Set<Resposta> respostas = submissaoService.buscarRespostasPorAgrupador(agrupador.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("formulario", formulario);
        response.put("respostas", respostas);
        response.put("dataSubmissao", agrupador.getDataFinalizacao());
        response.put("podeEditar", agrupador.podeEditar());

        return ResponseEntity.ok(response);
    }

    /**
     * RF12: Editar resposta (se formulário permitir)
     */
    @PutMapping("/submissao/{agrupadorId}/respostas/{respostaId}")
    @Auditavel(acao = "EDITAR_RESPOSTA", tipo = "SUBMISSAO")
    public ResponseEntity<Map<String, Object>> editarResposta(
            @PathVariable Integer agrupadorId,
            @PathVariable Integer respostaId,
            @RequestBody Map<String, Object> requestData,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario aluno = (Usuario) authentication.getPrincipal();

        RespostaAgrupador agrupador = submissaoService.buscarAgrupador(agrupadorId);

        // Verifica se pode editar
        if (!agrupador.podeEditar()) {
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Este formulário não permite edição de respostas");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Busca a resposta
        Resposta resposta = submissaoService.buscarResposta(respostaId);

        // Verifica se a resposta pertence ao agrupador
        if (!resposta.getRespostaAgrupador().getId().equals(agrupadorId)) {
            Map<String, Object> error = new HashMap<>();
            error.put("erro", "Resposta não pertence a esta submissão");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        // Atualiza a resposta
        if (resposta.getQuestao().getTipo() == TipoQuestao.ABERTA) {
            String textoResposta = (String) requestData.get("textoResposta");
            resposta = submissaoService.editarRespostaAberta(respostaId, textoResposta);
        } else {
            @SuppressWarnings("unchecked")
            List<Integer> alternativasIds = (List<Integer>) requestData.get("alternativasIds");
            resposta = submissaoService.editarRespostaMultiplaEscolha(
                    respostaId,
                    new HashSet<>(alternativasIds)
            );
        }

        logService.registrarSucesso(
                aluno,
                "EDITAR_RESPOSTA",
                "Resposta editada (ID: " + respostaId + ") no Agrupador (ID: " + agrupadorId + ")",
                "SUBMISSAO",
                request
        );

        Map<String, Object> response = new HashMap<>();
        response.put("mensagem", "Resposta atualizada com sucesso");
        response.put("resposta", resposta);

        return ResponseEntity.ok(response);
    }

    /**
     * Listar turmas disponíveis para o aluno responder
     */
    @GetMapping("/minhas-turmas")
    public ResponseEntity<List<Turma>> listarMinhasTurmas(Authentication authentication) {
        Usuario aluno = (Usuario) authentication.getPrincipal();

        List<Turma> turmas = turmaService.buscarPorAluno(aluno);

        return ResponseEntity.ok(turmas);
    }

    /**
     * Verificar status de submissão para um formulário
     */
    @GetMapping("/submissao/status/{formularioId}")
    public ResponseEntity<Map<String, Object>> verificarStatus(
            @PathVariable Integer formularioId,
            Authentication authentication) {

        Usuario aluno = (Usuario) authentication.getPrincipal();

        boolean jaRespondeu = submissaoService.jaRespondeu(formularioId, aluno.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("formularioId", formularioId);
        response.put("jaRespondeu", jaRespondeu);

        if (jaRespondeu) {
            Optional<RespostaAgrupador> agrupadorOpt = submissaoService.buscarSubmissaoDoUsuario(
                    formularioId,
                    aluno.getId()
            );

            if (agrupadorOpt.isPresent()) {
                RespostaAgrupador agrupador = agrupadorOpt.get();
                response.put("dataSubmissao", agrupador.getDataFinalizacao());
                response.put("podeEditar", agrupador.podeEditar());
            }
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Cancelar submissão em andamento (antes de finalizar)
     */
    @DeleteMapping("/submissao/{agrupadorId}/cancelar")
    @Auditavel(acao = "CANCELAR_SUBMISSAO", tipo = "SUBMISSAO")
    public ResponseEntity<Map<String, String>> cancelarSubmissao(
            @PathVariable Integer agrupadorId,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario aluno = (Usuario) authentication.getPrincipal();

        RespostaAgrupador agrupador = submissaoService.buscarAgrupador(agrupadorId);

        // Verifica se a submissão já foi finalizada
        if (agrupador.getFinalizada()) {
            Map<String, String> error = new HashMap<>();
            error.put("erro", "Não é possível cancelar uma submissão já finalizada");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        submissaoService.cancelarSubmissao(agrupadorId);

        logService.registrarSucesso(
                aluno,
                "CANCELAR_SUBMISSAO",
                "Submissão cancelada (Agrupador ID: " + agrupadorId + ")",
                "SUBMISSAO",
                request
        );

        Map<String, String> response = new HashMap<>();
        response.put("mensagem", "Submissão cancelada com sucesso");
        return ResponseEntity.ok(response);
    }
}
