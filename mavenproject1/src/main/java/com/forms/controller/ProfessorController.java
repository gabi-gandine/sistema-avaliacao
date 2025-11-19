package com.forms.controller;

import com.forms.models.*;
import com.forms.service.*;
import com.forms.service.annotation.Auditavel;
import com.forms.service.dto.RelatorioFormulario;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para funcionalidades do PROFESSOR
 *
 * Responsabilidades:
 * - RF07: Criar e gerenciar formulários de avaliação
 * - RF09: Criar e gerenciar questões (abertas, múltipla escolha, caixas de seleção, likert)
 * - RF11: Definir se formulário é anônimo ou não
 * - RF12: Habilitar/desabilitar edição de respostas
 * - RF16: Visualizar relatórios consolidados (apenas suas turmas)
 * - RF17: Visualizar scores calculados
 * - RF19: Respeitar anonimato nas visualizações
 *
 * Acesso: Apenas usuários com perfil PROFESSOR, COORDENADOR ou ADMINISTRADOR
 *
 * @author gabriela
 */
@RestController
@RequestMapping("/api/professor")
@PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADOR', 'ADMINISTRADOR')")
public class ProfessorController {

    @Autowired
    private FormularioService formularioService;

    @Autowired
    private QuestaoService questaoService;

    @Autowired
    private AlternativaService alternativaService;

    @Autowired
    private RelatorioService relatorioService;

    @Autowired
    private LogAuditoriaService logService;

    // ========== GESTÃO DE FORMULÁRIOS ==========

    /**
     * RF07: Listar formulários criados pelo professor
     */
    @GetMapping("/formularios")
    public ResponseEntity<List<Formulario>> listarMeusFormularios(Authentication authentication) {
        Usuario professor = (Usuario) authentication.getPrincipal();
        List<Formulario> formularios = formularioService.buscarPorCriador(professor);
        return ResponseEntity.ok(formularios);
    }

    /**
     * RF07: Buscar formulário por ID
     */
    @GetMapping("/formularios/{id}")
    public ResponseEntity<Formulario> buscarFormulario(@PathVariable Integer id) {
        Formulario formulario = formularioService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));
        return ResponseEntity.ok(formulario);
    }

    /**
     * RF07, RF11, RF12: Criar novo formulário
     */
    @PostMapping("/formularios")
    @Auditavel(acao = "CRIAR_FORMULARIO", tipo = "FORMULARIO")
    public ResponseEntity<Formulario> criarFormulario(
            @RequestBody Map<String, Object> requestData,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();

        String titulo = (String) requestData.get("titulo");
        String descricao = (String) requestData.get("descricao");
        Boolean isAnonimo = (Boolean) requestData.getOrDefault("isAnonimo", false);
        Boolean permiteEdicao = (Boolean) requestData.getOrDefault("permiteEdicao", false);

        Formulario formulario = formularioService.criar(titulo, descricao, professor);

        // RF11: Configurar anonimato
        if (isAnonimo != null && isAnonimo) {
            formulario = formularioService.definirAnonimato(formulario.getId(), true);
        }

        // RF12: Configurar permissão de edição
        if (permiteEdicao != null && permiteEdicao) {
            formulario = formularioService.habilitarEdicao(formulario.getId());
        }

        logService.registrarSucesso(
                professor,
                "CRIAR_FORMULARIO",
                "Formulário criado: " + formulario.getTitulo() + " (ID: " + formulario.getId() + ")",
                "FORMULARIO",
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(formulario);
    }

    /**
     * RF07: Atualizar formulário
     */
    @PutMapping("/formularios/{id}")
    @Auditavel(acao = "ATUALIZAR_FORMULARIO", tipo = "FORMULARIO")
    public ResponseEntity<Formulario> atualizarFormulario(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> requestData,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();

        String titulo = (String) requestData.get("titulo");
        String descricao = (String) requestData.get("descricao");

        Formulario formulario = formularioService.atualizar(id, titulo, descricao);

        logService.registrarSucesso(
                professor,
                "ATUALIZAR_FORMULARIO",
                "Formulário atualizado: " + formulario.getTitulo() + " (ID: " + formulario.getId() + ")",
                "FORMULARIO",
                request
        );

        return ResponseEntity.ok(formulario);
    }

    /**
     * RF11: Definir se formulário é anônimo
     */
    @PostMapping("/formularios/{id}/anonimato")
    @Auditavel(acao = "DEFINIR_ANONIMATO", tipo = "FORMULARIO")
    public ResponseEntity<Formulario> definirAnonimato(
            @PathVariable Integer id,
            @RequestParam Boolean isAnonimo,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();
        Formulario formulario = formularioService.definirAnonimato(id, isAnonimo);

        logService.registrarSucesso(
                professor,
                "DEFINIR_ANONIMATO",
                "Anonimato definido para Formulário (ID: " + id + "): " + isAnonimo,
                "FORMULARIO",
                request
        );

        return ResponseEntity.ok(formulario);
    }

    /**
     * RF12: Habilitar edição de respostas
     */
    @PostMapping("/formularios/{id}/habilitar-edicao")
    @Auditavel(acao = "HABILITAR_EDICAO", tipo = "FORMULARIO")
    public ResponseEntity<Formulario> habilitarEdicao(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();
        Formulario formulario = formularioService.habilitarEdicao(id);

        logService.registrarSucesso(
                professor,
                "HABILITAR_EDICAO",
                "Edição habilitada para Formulário (ID: " + id + ")",
                "FORMULARIO",
                request
        );

        return ResponseEntity.ok(formulario);
    }

    /**
     * RF12: Desabilitar edição de respostas
     */
    @PostMapping("/formularios/{id}/desabilitar-edicao")
    @Auditavel(acao = "DESABILITAR_EDICAO", tipo = "FORMULARIO")
    public ResponseEntity<Formulario> desabilitarEdicao(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();
        Formulario formulario = formularioService.desabilitarEdicao(id);

        logService.registrarSucesso(
                professor,
                "DESABILITAR_EDICAO",
                "Edição desabilitada para Formulário (ID: " + id + ")",
                "FORMULARIO",
                request
        );

        return ResponseEntity.ok(formulario);
    }

    /**
     * RF07: Ativar formulário
     */
    @PostMapping("/formularios/{id}/ativar")
    @Auditavel(acao = "ATIVAR_FORMULARIO", tipo = "FORMULARIO")
    public ResponseEntity<Formulario> ativarFormulario(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();
        Formulario formulario = formularioService.ativar(id);

        logService.registrarSucesso(
                professor,
                "ATIVAR_FORMULARIO",
                "Formulário ativado: " + formulario.getTitulo() + " (ID: " + formulario.getId() + ")",
                "FORMULARIO",
                request
        );

        return ResponseEntity.ok(formulario);
    }

    /**
     * RF07: Desativar formulário
     */
    @PostMapping("/formularios/{id}/desativar")
    @Auditavel(acao = "DESATIVAR_FORMULARIO", tipo = "FORMULARIO")
    public ResponseEntity<Formulario> desativarFormulario(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();
        Formulario formulario = formularioService.desativar(id);

        logService.registrarSucesso(
                professor,
                "DESATIVAR_FORMULARIO",
                "Formulário desativado: " + formulario.getTitulo() + " (ID: " + formulario.getId() + ")",
                "FORMULARIO",
                request
        );

        return ResponseEntity.ok(formulario);
    }

    /**
     * RF07: Deletar formulário
     */
    @DeleteMapping("/formularios/{id}")
    @Auditavel(acao = "DELETAR_FORMULARIO", tipo = "FORMULARIO")
    public ResponseEntity<Map<String, String>> deletarFormulario(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();
        Formulario formulario = formularioService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        formularioService.deletar(id);

        logService.registrarSucesso(
                professor,
                "DELETAR_FORMULARIO",
                "Formulário deletado: " + formulario.getTitulo() + " (ID: " + formulario.getId() + ")",
                "FORMULARIO",
                request
        );

        Map<String, String> response = new HashMap<>();
        response.put("mensagem", "Formulário deletado com sucesso");
        return ResponseEntity.ok(response);
    }

    // ========== GESTÃO DE QUESTÕES ==========

    /**
     * RF09: Listar questões de um formulário
     */
    @GetMapping("/formularios/{formularioId}/questoes")
    public ResponseEntity<List<Questao>> listarQuestoes(@PathVariable Integer formularioId) {
        List<Questao> questoes = questaoService.listarPorFormulario(formularioId);
        return ResponseEntity.ok(questoes);
    }

    /**
     * RF09: Criar nova questão
     */
    @PostMapping("/formularios/{formularioId}/questoes")
    @Auditavel(acao = "CRIAR_QUESTAO", tipo = "QUESTAO")
    public ResponseEntity<Questao> criarQuestao(
            @PathVariable Integer formularioId,
            @RequestBody Map<String, Object> requestData,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();

        String texto = (String) requestData.get("texto");
        String tipoStr = (String) requestData.get("tipo");
        Boolean obrigatoria = (Boolean) requestData.getOrDefault("obrigatoria", false);

        TipoQuestao tipo = TipoQuestao.valueOf(tipoStr);

        Questao questao = questaoService.criar(formularioId, texto, tipo, obrigatoria);

        logService.registrarSucesso(
                professor,
                "CRIAR_QUESTAO",
                "Questão criada no Formulário (ID: " + formularioId + ") - Tipo: " + tipo,
                "QUESTAO",
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(questao);
    }

    /**
     * RF09: Atualizar questão
     */
    @PutMapping("/questoes/{id}")
    @Auditavel(acao = "ATUALIZAR_QUESTAO", tipo = "QUESTAO")
    public ResponseEntity<Questao> atualizarQuestao(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> requestData,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();

        String texto = (String) requestData.get("texto");
        Boolean obrigatoria = (Boolean) requestData.get("obrigatoria");

        Questao questao = questaoService.atualizar(id, texto, obrigatoria);

        logService.registrarSucesso(
                professor,
                "ATUALIZAR_QUESTAO",
                "Questão atualizada (ID: " + id + ")",
                "QUESTAO",
                request
        );

        return ResponseEntity.ok(questao);
    }

    /**
     * RF09: Deletar questão
     */
    @DeleteMapping("/questoes/{id}")
    @Auditavel(acao = "DELETAR_QUESTAO", tipo = "QUESTAO")
    public ResponseEntity<Map<String, String>> deletarQuestao(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();

        questaoService.deletar(id);

        logService.registrarSucesso(
                professor,
                "DELETAR_QUESTAO",
                "Questão deletada (ID: " + id + ")",
                "QUESTAO",
                request
        );

        Map<String, String> response = new HashMap<>();
        response.put("mensagem", "Questão deletada com sucesso");
        return ResponseEntity.ok(response);
    }

    /**
     * RF09: Reordenar questões
     */
    @PostMapping("/formularios/{formularioId}/questoes/reordenar")
    @Auditavel(acao = "REORDENAR_QUESTOES", tipo = "QUESTAO")
    public ResponseEntity<List<Questao>> reordenarQuestoes(
            @PathVariable Integer formularioId,
            @RequestBody Map<Integer, Integer> novaOrdem,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();

        List<Questao> questoes = questaoService.reordenar(formularioId, novaOrdem);

        logService.registrarSucesso(
                professor,
                "REORDENAR_QUESTOES",
                "Questões reordenadas no Formulário (ID: " + formularioId + ")",
                "QUESTAO",
                request
        );

        return ResponseEntity.ok(questoes);
    }

    // ========== GESTÃO DE ALTERNATIVAS ==========

    /**
     * RF09: Listar alternativas de uma questão
     */
    @GetMapping("/questoes/{questaoId}/alternativas")
    public ResponseEntity<List<Alternativa>> listarAlternativas(@PathVariable Integer questaoId) {
        List<Alternativa> alternativas = alternativaService.listarPorQuestao(questaoId);
        return ResponseEntity.ok(alternativas);
    }

    /**
     * RF09: Criar nova alternativa
     */
    @PostMapping("/questoes/{questaoId}/alternativas")
    @Auditavel(acao = "CRIAR_ALTERNATIVA", tipo = "ALTERNATIVA")
    public ResponseEntity<Alternativa> criarAlternativa(
            @PathVariable Integer questaoId,
            @RequestBody Map<String, Object> requestData,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();

        String textoAlternativa = (String) requestData.get("textoAlternativa");
        BigDecimal peso = requestData.containsKey("peso")
                ? new BigDecimal(requestData.get("peso").toString())
                : null;
        Boolean isCorreta = (Boolean) requestData.getOrDefault("isCorreta", false);

        Alternativa alternativa = alternativaService.criar(questaoId, textoAlternativa, peso, isCorreta);

        logService.registrarSucesso(
                professor,
                "CRIAR_ALTERNATIVA",
                "Alternativa criada na Questão (ID: " + questaoId + ")",
                "ALTERNATIVA",
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(alternativa);
    }

    /**
     * RF09, RF17: Criar alternativa com peso Likert-5
     */
    @PostMapping("/questoes/{questaoId}/alternativas/likert")
    @Auditavel(acao = "CRIAR_ALTERNATIVA_LIKERT", tipo = "ALTERNATIVA")
    public ResponseEntity<Alternativa> criarAlternativaLikert(
            @PathVariable Integer questaoId,
            @RequestBody Map<String, Object> requestData,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();

        String textoAlternativa = (String) requestData.get("textoAlternativa");
        Integer posicaoLikert = (Integer) requestData.get("posicaoLikert"); // 1 a 5

        Alternativa alternativa = alternativaService.criarLikert(questaoId, textoAlternativa, posicaoLikert);

        logService.registrarSucesso(
                professor,
                "CRIAR_ALTERNATIVA_LIKERT",
                "Alternativa Likert-5 criada na Questão (ID: " + questaoId + ") - Posição: " + posicaoLikert,
                "ALTERNATIVA",
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(alternativa);
    }

    /**
     * RF09: Atualizar alternativa
     */
    @PutMapping("/alternativas/{id}")
    @Auditavel(acao = "ATUALIZAR_ALTERNATIVA", tipo = "ALTERNATIVA")
    public ResponseEntity<Alternativa> atualizarAlternativa(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> requestData,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();

        String textoAlternativa = (String) requestData.get("textoAlternativa");
        BigDecimal peso = requestData.containsKey("peso")
                ? new BigDecimal(requestData.get("peso").toString())
                : null;
        Boolean isCorreta = (Boolean) requestData.get("isCorreta");

        Alternativa alternativa = alternativaService.atualizar(id, textoAlternativa, peso, isCorreta);

        logService.registrarSucesso(
                professor,
                "ATUALIZAR_ALTERNATIVA",
                "Alternativa atualizada (ID: " + id + ")",
                "ALTERNATIVA",
                request
        );

        return ResponseEntity.ok(alternativa);
    }

    /**
     * RF09: Deletar alternativa
     */
    @DeleteMapping("/alternativas/{id}")
    @Auditavel(acao = "DELETAR_ALTERNATIVA", tipo = "ALTERNATIVA")
    public ResponseEntity<Map<String, String>> deletarAlternativa(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();

        alternativaService.deletar(id);

        logService.registrarSucesso(
                professor,
                "DELETAR_ALTERNATIVA",
                "Alternativa deletada (ID: " + id + ")",
                "ALTERNATIVA",
                request
        );

        Map<String, String> response = new HashMap<>();
        response.put("mensagem", "Alternativa deletada com sucesso");
        return ResponseEntity.ok(response);
    }

    // ========== RELATÓRIOS ==========

    /**
     * RF16, RF17, RF19: Visualizar relatório de formulário (apenas turmas do professor)
     */
    @GetMapping("/relatorios/formulario/{formularioId}")
    @Auditavel(acao = "ACESSAR_RELATORIO", tipo = "RELATORIO")
    public ResponseEntity<List<RelatorioFormulario>> visualizarRelatorio(
            @PathVariable Integer formularioId,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();

        // RF16: Professor vê apenas relatórios das turmas onde leciona
        List<RelatorioFormulario> relatorios = relatorioService.gerarRelatorioPorProfessor(
                professor.getId(),
                formularioId
        );

        logService.registrarAcessoRelatorio(
                professor,
                formularioId,
                "PROFESSOR",
                request
        );

        return ResponseEntity.ok(relatorios);
    }

    /**
     * RF16, RF17: Visualizar relatório de uma turma específica
     */
    @GetMapping("/relatorios/formulario/{formularioId}/turma/{turmaId}")
    @Auditavel(acao = "ACESSAR_RELATORIO_TURMA", tipo = "RELATORIO")
    public ResponseEntity<RelatorioFormulario> visualizarRelatorioPorTurma(
            @PathVariable Integer formularioId,
            @PathVariable Integer turmaId,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario professor = (Usuario) authentication.getPrincipal();

        RelatorioFormulario relatorio = relatorioService.gerarRelatorioPorTurma(formularioId, turmaId);

        logService.registrarAcessoRelatorio(
                professor,
                formularioId,
                "TURMA",
                request
        );

        return ResponseEntity.ok(relatorio);
    }

    /**
     * Visualizar estatísticas básicas de um formulário
     */
    @GetMapping("/relatorios/formulario/{formularioId}/estatisticas")
    public ResponseEntity<Map<String, Object>> visualizarEstatisticas(
            @PathVariable Integer formularioId) {

        Map<String, Object> estatisticas = relatorioService.gerarEstatisticasBasicas(formularioId);
        return ResponseEntity.ok(estatisticas);
    }
}
