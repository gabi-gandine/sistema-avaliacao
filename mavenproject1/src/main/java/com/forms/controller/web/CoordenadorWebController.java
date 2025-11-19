package com.forms.controller.web;

import com.forms.models.*;
import com.forms.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

/**
 * Controller MVC para interface do Coordenador
 *
 * RF04 - Gestão de cursos e UCs
 * RF05 - Processos avaliativos
 * RF06 - Gestão de turmas
 * RF16 - Relatórios consolidados
 */
@Controller
@RequestMapping("/coordenador")
public class CoordenadorWebController {

    @Autowired
    private TurmaService turmaService;

    @Autowired
    private ProcessoAvaliativoService processoService;

    @Autowired
    private FormularioService formularioService;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private UnidadeCurricularService unidadeCurricularService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PerfilService perfilService;

    /**
     * Dashboard do coordenador
     * Exibe estatísticas e resumo
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Usuario usuario = obterUsuarioLogado(authentication);

        // Estatísticas
        Long totalTurmas = (long) turmaService.listarTodas().size();
        Long totalProcessos = (long) processoService.listarTodos().size();
        Long totalFormularios = (long) formularioService.listarTodos().size();

        model.addAttribute("usuario", usuario);
        model.addAttribute("totalTurmas", totalTurmas);
        model.addAttribute("totalProcessos", totalProcessos);
        model.addAttribute("totalFormularios", totalFormularios);
        model.addAttribute("paginaTitulo", "Dashboard - Coordenador");

        return "coordenador/dashboard";
    }

    // ==================== GESTÃO DE TURMAS ====================

    /**
     * Lista todas as turmas
     * RF06
     */
    @GetMapping("/turmas")
    public String listarTurmas(Model model) {
        List<Turma> turmas = turmaService.listarTodas();

        model.addAttribute("turmas", turmas);
        model.addAttribute("paginaTitulo", "Gestão de Turmas");

        return "coordenador/turmas/lista";
    }

    /**
     * Exibe formulário para criar nova turma
     * RF06
     */
    @GetMapping("/turmas/nova")
    public String criarTurma(Model model) {
        // Buscar UCs e professores para os selects
        List<UnidadeCurricular> unidades = unidadeCurricularService.listarTodas();
        List<Usuario> professores = usuarioService.listarPorPerfil(
                perfilService.buscarPorNome("PROFESSOR").orElse(null)
        );
        List<Usuario> alunos = usuarioService.listarPorPerfil(
                perfilService.buscarPorNome("ALUNO").orElse(null)
        );

        model.addAttribute("turma", new Turma());
        model.addAttribute("unidades", unidades);
        model.addAttribute("professores", professores);
        model.addAttribute("alunos", alunos);
        model.addAttribute("paginaTitulo", "Nova Turma");

        return "coordenador/turmas/criar";
    }

    /**
     * Exibe formulário para editar turma
     * RF06
     */
    @GetMapping("/turmas/{id}/editar")
    public String editarTurma(@PathVariable Integer id, Model model) {
        Turma turma = turmaService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));

        List<UnidadeCurricular> unidades = unidadeCurricularService.listarTodas();
        List<Usuario> professores = usuarioService.listarPorPerfil(
                perfilService.buscarPorNome("PROFESSOR").orElse(null)
        );
        List<Usuario> alunos = usuarioService.listarPorPerfil(
                perfilService.buscarPorNome("ALUNO").orElse(null)
        );

        model.addAttribute("turma", turma);
        model.addAttribute("unidades", unidades);
        model.addAttribute("professores", professores);
        model.addAttribute("alunos", alunos);
        model.addAttribute("paginaTitulo", "Editar Turma");

        return "coordenador/turmas/editar";
    }

    /**
     * Gerenciar alunos de uma turma
     * RF06
     */
    @GetMapping("/turmas/{id}/alunos")
    public String gerenciarAlunos(@PathVariable Integer id, Model model) {
        Turma turma = turmaService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));

        // Buscar todos os alunos disponíveis
        List<Usuario> todosAlunos = usuarioService.listarPorPerfil(
                perfilService.buscarPorNome("ALUNO").orElse(null)
        );

        model.addAttribute("turma", turma);
        model.addAttribute("alunosDaTurma", turma.getAlunos());
        model.addAttribute("todosAlunos", todosAlunos);
        model.addAttribute("paginaTitulo", "Gerenciar Alunos - Turma " + turma.getId());

        return "coordenador/turmas/alunos";
    }

    // ==================== GESTÃO DE PROCESSOS ====================

    /**
     * Exibe formulário para criar processo avaliativo
     * RF05
     */
    @GetMapping("/processos/novo")
    public String criarProcesso(Model model) {
        List<Curso> cursos = cursoService.listarTodos();
        List<UnidadeCurricular> unidades = unidadeCurricularService.listarTodas();

        model.addAttribute("processo", new ProcessoAvaliativo());
        model.addAttribute("cursos", cursos);
        model.addAttribute("unidades", unidades);
        model.addAttribute("paginaTitulo", "Novo Processo Avaliativo");

        return "coordenador/processos/criar";
    }

    /**
     * Exibe formulário para editar processo
     * RF05
     */
    @GetMapping("/processos/{id}/editar")
    public String editarProcesso(@PathVariable Integer id, Model model) {
        ProcessoAvaliativo processo = processoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));

        List<Curso> cursos = cursoService.listarTodos();
        List<UnidadeCurricular> unidades = unidadeCurricularService.listarTodas();

        model.addAttribute("processo", processo);
        model.addAttribute("cursos", cursos);
        model.addAttribute("unidades", unidades);
        model.addAttribute("paginaTitulo", "Editar Processo");

        return "coordenador/processos/editar";
    }

    /**
     * Vincular formulários a um processo
     * RF07
     */
    @GetMapping("/processos/{id}/formularios")
    public String vincularFormularios(@PathVariable Integer id, Model model) {
        ProcessoAvaliativo processo = processoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));

        // Todos os formulários disponíveis
        List<Formulario> todosFormularios = formularioService.listarTodos();

        // Formulários já vinculados
        List<Formulario> formulariosVinculados = formularioService.listarPorProcesso(processo);

        model.addAttribute("processo", processo);
        model.addAttribute("todosFormularios", todosFormularios);
        model.addAttribute("formulariosVinculados", formulariosVinculados);
        model.addAttribute("paginaTitulo", "Vincular Formulários - " + processo.getNome());

        return "coordenador/processos/vincular-formularios";
    }

    /**
     * Detalhes de uma turma
     * RF06
     */
    @GetMapping("/turmas/{id}")
    public String detalhesTurma(@PathVariable Integer id, Model model) {
        Turma turma = turmaService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));

        model.addAttribute("turma", turma);
        model.addAttribute("alunos", turma.getAlunos());
        model.addAttribute("professores", turma.getProfessores());
        model.addAttribute("paginaTitulo", "Detalhes da Turma");

        return "coordenador/turmas/detalhes";
    }

    // ==================== GESTÃO DE CURSOS ====================

    /**
     * Gestão de cursos
     * RF04
     */

    @GetMapping("/gestao/cursos")
    public String gestaoCursos(Model model) {
        List<Curso> cursos = cursoService.listarTodos();
        List<UnidadeCurricular> unidades = unidadeCurricularService.listarTodas();

        model.addAttribute("cursos", cursos);
        model.addAttribute("ucs", unidades); 

        model.addAttribute("novoCurso", new Curso());
        model.addAttribute("novaUC", new UnidadeCurricular());

        model.addAttribute("paginaTitulo", "Gestão de Cursos e Unidades Curriculares");

        return "coordenador/gestaoCursos";
    }

    /**
     * Lista processos avaliativos
     * RF05
     */
    @GetMapping("/processos")
    public String listarProcessos(Model model) {
        List<ProcessoAvaliativo> processos = processoService.listarTodos();

        model.addAttribute("processos", processos);
        model.addAttribute("paginaTitulo", "Processos Avaliativos");

        return "coordenador/processos/lista";
    }

    /**
     * Salva ou atualiza um Curso (RF04)
     */
    @PostMapping("/curso/salvar")
    public String salvarCurso(@ModelAttribute Curso novoCurso, RedirectAttributes ra) {
        try {
            cursoService.salvar(novoCurso); 
            ra.addFlashAttribute("mensagemSucesso", "Curso salvo com sucesso!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("mensagemErro", "Erro ao salvar curso: " + e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("mensagemErro", "Erro interno ao salvar curso: " + e.getMessage());
        }
        return "redirect:/coordenador/gestao/cursos";
    }

    /**
     * DELETAR CURSO
     */
    @PostMapping("/curso/deletar/{id}")
    public String deletarCurso(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            cursoService.deletar(id);
            ra.addFlashAttribute("mensagemSucesso", "Curso deletado com sucesso!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("mensagemErro", "Erro ao deletar curso: " + e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("mensagemErro", "Erro interno ao deletar curso: " + e.getMessage());
        }
        return "redirect:/coordenador/gestao/cursos";
    }
    
    /**
     * Salva ou atualiza uma Unidade Curricular (RF04)
     * Recebe o ID do curso separadamente (@RequestParam)
     */
    @PostMapping("/uc/salvar")
    public String salvarUC(@ModelAttribute UnidadeCurricular novaUC, @RequestParam Integer cursoId, RedirectAttributes ra) {
        try {
            Curso curso = cursoService.buscarPorId(cursoId)
                    .orElseThrow(() -> new IllegalArgumentException("Curso não encontrado para vincular a UC."));

            novaUC.setCurso(curso);
            
            unidadeCurricularService.salvar(novaUC);
            ra.addFlashAttribute("mensagemSucesso", "Unidade Curricular salva com sucesso!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("mensagemErro", "Erro ao salvar UC: " + e.getMessage());
        } catch (Exception e) {
             ra.addFlashAttribute("mensagemErro", "Erro interno ao salvar UC: " + e.getMessage());
        }
        return "redirect:/coordenador/gestao/cursos";
    }

    /**
     * DELETAR UNIDADE CURRICULAR
     */
    @PostMapping("/uc/deletar/{id}")
    public String deletarUC(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            unidadeCurricularService.deletar(id);
            ra.addFlashAttribute("mensagemSucesso", "Unidade Curricular deletada com sucesso!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("mensagemErro", "Erro ao deletar UC: " + e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("mensagemErro", "Erro interno ao deletar UC: " + e.getMessage());
        }
        return "redirect:/coordenador/gestao/cursos";
    }

    /**
     * Processa a criação de nova turma
     * RF06
     */
    @PostMapping("/turmas/salvar")
    public String salvarTurma(
            @RequestParam Integer ano, 
            @RequestParam Integer semestre,
            @RequestParam Integer ucId,
            @RequestParam(required = false) List<Integer> professoresIds,
            @RequestParam(required = false) List<Integer> alunosIds,
            RedirectAttributes ra) {
        try {
            if (ano == null || semestre == null || ucId == null) {
                 throw new IllegalArgumentException("Ano, Semestre e Unidade Curricular são obrigatórios.");
            }
            
            Turma novaTurma = new Turma();
            novaTurma.setAno(ano);
            novaTurma.setSemestre(semestre);
            
            UnidadeCurricular uc = unidadeCurricularService.buscarPorId(ucId)
                    .orElseThrow(() -> new IllegalArgumentException("Unidade Curricular não encontrada."));
            novaTurma.setUc(uc);
            
            if (professoresIds != null && !professoresIds.isEmpty()) {
                Set<Usuario> professores = new java.util.HashSet<>();
                for (Integer id : professoresIds) {
                    usuarioService.buscarPorId(id).ifPresent(professores::add);
                }
                novaTurma.setProfessores(professores);
            }

            if (alunosIds != null && !alunosIds.isEmpty()) {
                Set<Usuario> alunos = new java.util.HashSet<>();
                for (Integer id : alunosIds) {
                    usuarioService.buscarPorId(id).ifPresent(alunos::add);
                }
                novaTurma.setAlunos(alunos);
            }

            turmaService.salvar(novaTurma);

            ra.addFlashAttribute("mensagemSucesso", "Turma criada com sucesso!");
            return "redirect:/coordenador/turmas";
            
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("mensagemErro", "Erro ao criar turma: " + e.getMessage());
            return "redirect:/coordenador/turmas/nova";
        } catch (Exception e) {
            ra.addFlashAttribute("mensagemErro", "Erro interno ao criar turma. Detalhe: " + e.getMessage());
            return "redirect:/coordenador/turmas/nova";
        }
    }

    // ==================== RELATÓRIOS ====================

    /**
     * Relatórios consolidados
     * RF16
     */
    @GetMapping("/relatorios")
    public String relatoriosConsolidados(@RequestParam(required = false) Integer processoId,
                                        @RequestParam(required = false) Integer cursoId,
                                        Model model) {
        List<ProcessoAvaliativo> processos = processoService.listarTodos();
        List<Curso> cursos = cursoService.listarTodos();

        model.addAttribute("processos", processos);
        model.addAttribute("cursos", cursos);
        model.addAttribute("processoId", processoId);
        model.addAttribute("cursoId", cursoId);
        model.addAttribute("paginaTitulo", "Relatórios Consolidados");

        return "coordenador/relatorios/consolidados";
    }

    /**
     * Helper method para obter usuário logado
     */
    private Usuario obterUsuarioLogado(Authentication authentication) {
        String email = authentication.getName();
        return usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado"));
    }
}
