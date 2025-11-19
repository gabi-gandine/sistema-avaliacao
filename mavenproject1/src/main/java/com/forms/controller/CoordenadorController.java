package com.forms.controller;

import com.forms.models.*;
import com.forms.service.*;
import com.forms.service.annotation.Auditavel;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller para funcionalidades do COORDENADOR
 *
 * Responsabilidades:
 * - RF05: Gerenciar Processos Avaliativos
 * - RF06: Gerenciar Cursos, Disciplinas e Turmas
 * - RF07: Vincular formulários a processos e definir perfis-alvo
 * - RF08: Ativar/desativar processos avaliativos
 *
 * Acesso: Apenas usuários com perfil COORDENADOR ou ADMINISTRADOR
 *
 * @author gabriela
 */
@RestController
@RequestMapping("/api/coordenador")
@PreAuthorize("hasAnyRole('COORDENADOR', 'ADMINISTRADOR')")
public class CoordenadorController {

    @Autowired
    private ProcessoAvaliativoService processoService;

    @Autowired
    private FormularioService formularioService;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private UnidadeCurricularService unidadeCurricularService;

    @Autowired
    private TurmaService turmaService;

    @Autowired
    private LogAuditoriaService logService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * RF05: Listar todos os processos avaliativos
     */
    @GetMapping("/processos")
    public ResponseEntity<List<ProcessoAvaliativo>> listarProcessos() {
        List<ProcessoAvaliativo> processos = processoService.listarTodos();
        return ResponseEntity.ok(processos);
    }

    /**
     * RF05: Buscar processo por ID
     */
    @GetMapping("/processos/{id}")
    public ResponseEntity<ProcessoAvaliativo> buscarProcesso(@PathVariable Integer id) {
        ProcessoAvaliativo processo = processoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));
        return ResponseEntity.ok(processo);
    }

    /**
     * RF05: Criar novo processo avaliativo
     */
    @PostMapping("/processos")
    @Auditavel(acao = "CRIAR_PROCESSO", tipo = "PROCESSO_AVALIATIVO")
    public ResponseEntity<ProcessoAvaliativo> criarProcesso(
            @RequestBody ProcessoAvaliativo processo,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();

        ProcessoAvaliativo novoProcesso = processoService.criar(
                processo.getNome(),
                processo.getDescricao(),
                processo.getDataInicio(),
                processo.getDataFim()
        );

        logService.registrarSucesso(
                coordenador,
                "CRIAR_PROCESSO",
                "Processo criado: " + novoProcesso.getNome() + " (ID: " + novoProcesso.getId() + ")",
                "PROCESSO_AVALIATIVO",
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(novoProcesso);
    }

    /**
     * RF05: Atualizar processo avaliativo
     */
    @PutMapping("/processos/{id}")
    @Auditavel(acao = "ATUALIZAR_PROCESSO", tipo = "PROCESSO_AVALIATIVO")
    public ResponseEntity<ProcessoAvaliativo> atualizarProcesso(
            @PathVariable Integer id,
            @RequestBody ProcessoAvaliativo processoAtualizado,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();

        ProcessoAvaliativo processo = processoService.atualizar(
                id,
                processoAtualizado.getNome(),
                processoAtualizado.getDescricao(),
                processoAtualizado.getDataInicio(),
                processoAtualizado.getDataFim()
        );

        logService.registrarSucesso(
                coordenador,
                "ATUALIZAR_PROCESSO",
                "Processo atualizado: " + processo.getNome() + " (ID: " + processo.getId() + ")",
                "PROCESSO_AVALIATIVO",
                request
        );

        return ResponseEntity.ok(processo);
    }

    /**
     * RF08: Ativar processo avaliativo
     */
    @PostMapping("/processos/{id}/ativar")
    @Auditavel(acao = "ATIVAR_PROCESSO", tipo = "PROCESSO_AVALIATIVO")
    public ResponseEntity<ProcessoAvaliativo> ativarProcesso(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();
        ProcessoAvaliativo processo = processoService.ativar(id);

        logService.registrarSucesso(
                coordenador,
                "ATIVAR_PROCESSO",
                "Processo ativado: " + processo.getNome() + " (ID: " + processo.getId() + ")",
                "PROCESSO_AVALIATIVO",
                request
        );

        return ResponseEntity.ok(processo);
    }

    /**
     * RF08: Desativar processo avaliativo
     */
    @PostMapping("/processos/{id}/desativar")
    @Auditavel(acao = "DESATIVAR_PROCESSO", tipo = "PROCESSO_AVALIATIVO")
    public ResponseEntity<ProcessoAvaliativo> desativarProcesso(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();
        ProcessoAvaliativo processo = processoService.desativar(id);

        logService.registrarSucesso(
                coordenador,
                "DESATIVAR_PROCESSO",
                "Processo desativado: " + processo.getNome() + " (ID: " + processo.getId() + ")",
                "PROCESSO_AVALIATIVO",
                request
        );

        return ResponseEntity.ok(processo);
    }

    /**
     * RF05: Deletar processo avaliativo
     */
    @DeleteMapping("/processos/{id}")
    @Auditavel(acao = "DELETAR_PROCESSO", tipo = "PROCESSO_AVALIATIVO")
    public ResponseEntity<Map<String, String>> deletarProcesso(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();
        ProcessoAvaliativo processo = processoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));

        processoService.deletar(id);

        logService.registrarSucesso(
                coordenador,
                "DELETAR_PROCESSO",
                "Processo deletado: " + processo.getNome() + " (ID: " + processo.getId() + ")",
                "PROCESSO_AVALIATIVO",
                request
        );

        Map<String, String> response = new HashMap<>();
        response.put("mensagem", "Processo avaliativo deletado com sucesso");
        return ResponseEntity.ok(response);
    }

    /**
     * RF07: Vincular formulário ao processo
     */
    @PostMapping("/processos/{processoId}/formularios/{formularioId}")
    @Auditavel(acao = "VINCULAR_FORMULARIO_PROCESSO", tipo = "PROCESSO_AVALIATIVO")
    public ResponseEntity<ProcessoAvaliativo> vincularFormulario(
            @PathVariable Integer processoId,
            @PathVariable Integer formularioId,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();

        ProcessoAvaliativo processo = processoService.vincularFormulario(processoId, formularioId);

        logService.registrarSucesso(
                coordenador,
                "VINCULAR_FORMULARIO_PROCESSO",
                "Formulário (ID: " + formularioId + ") vinculado ao Processo (ID: " + processoId + ")",
                "PROCESSO_AVALIATIVO",
                request
        );

        return ResponseEntity.ok(processo);
    }

    /**
     * RF07: Desvincular formulário do processo
     */
    @DeleteMapping("/processos/{processoId}/formularios/{formularioId}")
    @Auditavel(acao = "DESVINCULAR_FORMULARIO_PROCESSO", tipo = "PROCESSO_AVALIATIVO")
    public ResponseEntity<ProcessoAvaliativo> desvincularFormulario(
            @PathVariable Integer processoId,
            @PathVariable Integer formularioId,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();

        ProcessoAvaliativo processo = processoService.desvincularFormulario(processoId, formularioId);

        logService.registrarSucesso(
                coordenador,
                "DESVINCULAR_FORMULARIO_PROCESSO",
                "Formulário (ID: " + formularioId + ") desvinculado do Processo (ID: " + processoId + ")",
                "PROCESSO_AVALIATIVO",
                request
        );

        return ResponseEntity.ok(processo);
    }

    /**
     * RF07: Definir perfis-alvo para um formulário
     */
    @PostMapping("/formularios/{formularioId}/perfis-destino")
    @Auditavel(acao = "DEFINIR_PERFIS_DESTINO", tipo = "FORMULARIO")
    public ResponseEntity<Formulario> definirPerfisDestino(
            @PathVariable Integer formularioId,
            @RequestBody Set<Integer> perfisIds,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();

        Formulario formulario = formularioService.definirPerfisDestino(formularioId, perfisIds);

        logService.registrarSucesso(
                coordenador,
                "DEFINIR_PERFIS_DESTINO",
                "Perfis-alvo definidos para Formulário (ID: " + formularioId + "): " + perfisIds,
                "FORMULARIO",
                request
        );

        return ResponseEntity.ok(formulario);
    }

    /**
     * RF05: Listar processos ativos no período atual
     */
    @GetMapping("/processos/ativos")
    public ResponseEntity<List<ProcessoAvaliativo>> listarProcessosAtivos() {
        List<ProcessoAvaliativo> processos = processoService.listarAtivos();
        return ResponseEntity.ok(processos);
    }

    /**
     * RF05: Buscar processos por período
     */
    @GetMapping("/processos/periodo")
    public ResponseEntity<List<ProcessoAvaliativo>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        List<ProcessoAvaliativo> processos = processoService.buscarPorPeriodo(inicio, fim);
        return ResponseEntity.ok(processos);
    }

    // ========== GESTÃO DE CURSOS, DISCIPLINAS E TURMAS ==========

    /**
     * RF06: Listar todos os cursos
     */
    @GetMapping("/cursos")
    public ResponseEntity<List<Curso>> listarCursos() {
        List<Curso> cursos = cursoService.listarTodos();
        return ResponseEntity.ok(cursos);
    }

    /**
     * RF06: Criar novo curso
     */
    @PostMapping("/cursos")
    @Auditavel(acao = "CRIAR_CURSO", tipo = "CURSO")
    public ResponseEntity<Curso> criarCurso(
            @RequestBody Curso curso,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();
        Curso novoCurso = cursoService.criar(curso);

        logService.registrarSucesso(
                coordenador,
                "CRIAR_CURSO",
                "Curso criado: " + novoCurso.getNome() + " (ID: " + novoCurso.getId() + ")",
                "CURSO",
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(novoCurso);
    }

    /**
     * RF06: Atualizar curso
     */
    @PutMapping("/cursos/{id}")
    @Auditavel(acao = "ATUALIZAR_CURSO", tipo = "CURSO")
    public ResponseEntity<Curso> atualizarCurso(
            @PathVariable Integer id,
            @RequestBody Curso curso,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();
        Curso cursoAtualizado = cursoService.atualizar(id, curso);

        logService.registrarSucesso(
                coordenador,
                "ATUALIZAR_CURSO",
                "Curso atualizado: " + cursoAtualizado.getNome() + " (ID: " + cursoAtualizado.getId() + ")",
                "CURSO",
                request
        );

        return ResponseEntity.ok(cursoAtualizado);
    }

    /**
     * RF06: Deletar curso
     */
    @DeleteMapping("/cursos/{id}")
    @Auditavel(acao = "DELETAR_CURSO", tipo = "CURSO")
    public ResponseEntity<Map<String, String>> deletarCurso(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();
        Curso curso = cursoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso não encontrado"));

        cursoService.deletar(id);

        logService.registrarSucesso(
                coordenador,
                "DELETAR_CURSO",
                "Curso deletado: " + curso.getNome() + " (ID: " + curso.getId() + ")",
                "CURSO",
                request
        );

        Map<String, String> response = new HashMap<>();
        response.put("mensagem", "Curso deletado com sucesso");
        return ResponseEntity.ok(response);
    }

    /**
     * RF06: Listar todas as unidades curriculares
     */
    @GetMapping("/unidades-curriculares")
    public ResponseEntity<List<UnidadeCurricular>> listarUnidadesCurriculares() {
        List<UnidadeCurricular> unidades = unidadeCurricularService.listarTodas();
        return ResponseEntity.ok(unidades);
    }

    /**
     * RF06: Criar nova unidade curricular
     */
    @PostMapping("/unidades-curriculares")
    @Auditavel(acao = "CRIAR_UNIDADE_CURRICULAR", tipo = "UNIDADE_CURRICULAR")
    public ResponseEntity<UnidadeCurricular> criarUnidadeCurricular(
            @RequestBody UnidadeCurricular unidadeCurricular,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();
        UnidadeCurricular novaUnidade = unidadeCurricularService.criar(unidadeCurricular);

        logService.registrarSucesso(
                coordenador,
                "CRIAR_UNIDADE_CURRICULAR",
                "Unidade Curricular criada: " + novaUnidade.getNome() + " (ID: " + novaUnidade.getId() + ")",
                "UNIDADE_CURRICULAR",
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(novaUnidade);
    }

    /**
     * RF06: Atualizar unidade curricular
     */
    @PutMapping("/unidades-curriculares/{id}")
    @Auditavel(acao = "ATUALIZAR_UNIDADE_CURRICULAR", tipo = "UNIDADE_CURRICULAR")
    public ResponseEntity<UnidadeCurricular> atualizarUnidadeCurricular(
            @PathVariable Integer id,
            @RequestBody UnidadeCurricular unidadeCurricular,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();
        UnidadeCurricular unidadeAtualizada = unidadeCurricularService.atualizar(id, unidadeCurricular);

        logService.registrarSucesso(
                coordenador,
                "ATUALIZAR_UNIDADE_CURRICULAR",
                "Unidade Curricular atualizada: " + unidadeAtualizada.getNome() + " (ID: " + unidadeAtualizada.getId() + ")",
                "UNIDADE_CURRICULAR",
                request
        );

        return ResponseEntity.ok(unidadeAtualizada);
    }

    /**
     * RF06: Deletar unidade curricular
     */
    @DeleteMapping("/unidades-curriculares/{id}")
    @Auditavel(acao = "DELETAR_UNIDADE_CURRICULAR", tipo = "UNIDADE_CURRICULAR")
    public ResponseEntity<Map<String, String>> deletarUnidadeCurricular(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();
        UnidadeCurricular unidadeCurricular = unidadeCurricularService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Unidade Curricular não encontrada"));

        unidadeCurricularService.deletar(id);

        logService.registrarSucesso(
                coordenador,
                "DELETAR_UNIDADE_CURRICULAR",
                "Unidade Curricular deletada: " + unidadeCurricular.getNome() + " (ID: " + unidadeCurricular.getId() + ")",
                "UNIDADE_CURRICULAR",
                request
        );

        Map<String, String> response = new HashMap<>();
        response.put("mensagem", "Unidade Curricular deletada com sucesso");
        return ResponseEntity.ok(response);
    }

    /**
     * RF06: Listar todas as turmas
     */
    @GetMapping("/turmas")
    public ResponseEntity<List<Turma>> listarTurmas() {
        List<Turma> turmas = turmaService.listarTodas();
        return ResponseEntity.ok(turmas);
    }

    /**
     * RF06: Criar nova turma
     */
    @PostMapping("/turmas")
    @Auditavel(acao = "CRIAR_TURMA", tipo = "TURMA")
    public ResponseEntity<Turma> criarTurma(
            @RequestBody Turma turma,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();
        Turma novaTurma = turmaService.criar(turma);

        logService.registrarSucesso(
                coordenador,
                "CRIAR_TURMA",
                "Turma criada: " + novaTurma.getNome() + " (ID: " + novaTurma.getId() + ")",
                "TURMA",
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(novaTurma);
    }

    /**
     * RF06: Atualizar turma
     */
    @PutMapping("/turmas/{id}")
    @Auditavel(acao = "ATUALIZAR_TURMA", tipo = "TURMA")
    public ResponseEntity<Turma> atualizarTurma(
            @PathVariable Integer id,
            @RequestBody Turma turma,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();
        Turma turmaAtualizada = turmaService.atualizar(id, turma);

        logService.registrarSucesso(
                coordenador,
                "ATUALIZAR_TURMA",
                "Turma atualizada: " + turmaAtualizada.getNome() + " (ID: " + turmaAtualizada.getId() + ")",
                "TURMA",
                request
        );

        return ResponseEntity.ok(turmaAtualizada);
    }

    /**
     * RF05: Vincular professor à turma
     */
    @PostMapping("/turmas/{turmaId}/professores/{professorId}")
    @Auditavel(acao = "VINCULAR_PROFESSOR_TURMA", tipo = "TURMA")
    public ResponseEntity<Turma> vincularProfessor(
            @PathVariable Integer turmaId,
            @PathVariable Integer professorId,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();
        Usuario professor = usuarioService.buscarPorId(professorId)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado"));
        Turma turma = turmaService.vincularProfessor(turmaId, professor);

        logService.registrarSucesso(
                coordenador,
                "VINCULAR_PROFESSOR_TURMA",
                "Professor (ID: " + professorId + ") vinculado à Turma (ID: " + turmaId + ")",
                "TURMA",
                request
        );

        return ResponseEntity.ok(turma);
    }

    /**
     * RF05: Desvincular professor da turma
     */
    @DeleteMapping("/turmas/{turmaId}/professores/{professorId}")
    @Auditavel(acao = "DESVINCULAR_PROFESSOR_TURMA", tipo = "TURMA")
    public ResponseEntity<Turma> desvincularProfessor(
            @PathVariable Integer turmaId,
            @PathVariable Integer professorId,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();
        Turma turma = turmaService.desvincularProfessor(turmaId, professorId);

        logService.registrarSucesso(
                coordenador,
                "DESVINCULAR_PROFESSOR_TURMA",
                "Professor (ID: " + professorId + ") desvinculado da Turma (ID: " + turmaId + ")",
                "TURMA",
                request
        );

        return ResponseEntity.ok(turma);
    }

    /**
     * RF06: Deletar turma
     */
    @DeleteMapping("/turmas/{id}")
    @Auditavel(acao = "DELETAR_TURMA", tipo = "TURMA")
    public ResponseEntity<Map<String, String>> deletarTurma(
            @PathVariable Integer id,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();
        Turma turma = turmaService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));

        turmaService.deletar(id);

        logService.registrarSucesso(
                coordenador,
                "DELETAR_TURMA",
                "Turma deletada: " + turma.getNome() + " (ID: " + turma.getId() + ")",
                "TURMA",
                request
        );

        Map<String, String> response = new HashMap<>();
        response.put("mensagem", "Turma deletada com sucesso");
        return ResponseEntity.ok(response);
    }
}
