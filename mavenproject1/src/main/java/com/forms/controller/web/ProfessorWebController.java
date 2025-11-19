package com.forms.controller.web;

import com.forms.models.Formulario;
import com.forms.models.Turma;
import com.forms.models.Usuario;
import com.forms.models.Questao;
import com.forms.models.Alternativa;
import com.forms.models.TipoQuestao;
import com.forms.models.ProcessoAvaliativo;
import com.forms.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller MVC para interface do Professor
 *
 * RF07 - Criação e gestão de formulários
 * RF16 - Visualização de relatórios
 */
@Controller
@RequestMapping("/professor")
public class ProfessorWebController {

    @Autowired
    private FormularioService formularioService;

    @Autowired
    private TurmaService turmaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProcessoAvaliativoService processoService;

    @Autowired
    private QuestaoService questaoService;

    @Autowired
    private UnidadeCurricularService unidadeCurricularService;

    @Autowired
    private AlternativaService alternativaService;

    // ==================== DASHBOARD ====================

    /**
     * Dashboard do professor
     * Exibe formulários criados e turmas
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Usuario usuario = obterUsuarioLogado(authentication);

        // Buscar formulários do professor
        List<Formulario> formulariosProf = formularioService.listarPorCriador(usuario);

        // Buscar turmas do professor
        List<Turma> turmasProf = turmaService.buscarPorProfessor(usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("formularios", formulariosProf);
        model.addAttribute("turmas", turmasProf);
        model.addAttribute("totalFormularios", formulariosProf.size());
        model.addAttribute("totalTurmas", turmasProf.size());
        model.addAttribute("paginaTitulo", "Dashboard - Professor");

        return "professor/dashboard";
    }

    // ==================== FORMULÁRIOS ====================

    /**
     * Lista formulários do professor
     */
    @GetMapping("/formularios")
    public String listarFormularios(Authentication authentication, Model model) {
        Usuario usuario = obterUsuarioLogado(authentication);

        List<Formulario> formularios = formularioService.listarPorCriador(usuario);

        model.addAttribute("formularios", formularios);
        model.addAttribute("paginaTitulo", "Meus Formulários");

        return "professor/formularios";
    }

    /**
     * Exibe formulário para criar novo formulário
     * RF07
     */
    @GetMapping("/formularios/novo")
    public String novoFormulario(Authentication authentication, Model model) {
        Usuario usuario = obterUsuarioLogado(authentication);

        // Buscar processos avaliativos para vincular
        var processos = processoService.listarTodos();

        // Buscar turmas do professor
        List<Turma> turmas = turmaService.buscarPorProfessor(usuario);

        model.addAttribute("formulario", new Formulario());
        model.addAttribute("processos", processos);
        model.addAttribute("turmas", turmas);
        model.addAttribute("paginaTitulo", "Criar Novo Formulário");

        return "professor/criar-formulario";
    }

    /**
     * Exibe formulário para edição
     */
    @GetMapping("/formularios/{id}/editar")
    public String editarFormulario(@PathVariable Integer id,
                                   Authentication authentication,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        Usuario usuario = obterUsuarioLogado(authentication);

        Formulario formulario = formularioService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        // Verificar se o professor é o criador
        if (!formulario.getCriador().getId().equals(usuario.getId())) {
            redirectAttributes.addFlashAttribute("erro", "Você não tem permissão para editar este formulário");
            return "redirect:/professor/formularios";
        }

        model.addAttribute("formulario", formulario);
        model.addAttribute("questoes", formulario.getQuestoes());
        model.addAttribute("paginaTitulo", "Editar Formulário");

        return "professor/formularios/editar";
    }

    /**
     * Gerenciar questões de um formulário
     * RF08, RF09, RF10
     */
    @GetMapping("/formularios/{id}/questoes")
    public String gerenciarQuestoes(@PathVariable Integer id,
                                    Authentication authentication,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        Usuario usuario = obterUsuarioLogado(authentication);

        Formulario formulario = formularioService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        // Verificar se o professor é o criador
        if (!formulario.getCriador().getId().equals(usuario.getId())) {
            redirectAttributes.addFlashAttribute("erro", "Você não tem permissão para gerenciar questões deste formulário");
            return "redirect:/professor/formularios";
        }

        model.addAttribute("formulario", formulario);
        model.addAttribute("questoes", formulario.getQuestoes());
        model.addAttribute("paginaTitulo", "Gerenciar Questões - " + formulario.getTitulo());

        return "professor/gerenciar-questoes";
    }

    /**
     * Duplicar formulário existente
     */
    @GetMapping("/formularios/{id}/duplicar")
    public String duplicarFormulario(@PathVariable Integer id,
                                     Authentication authentication,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        Usuario usuario = obterUsuarioLogado(authentication);

        Formulario formularioOriginal = formularioService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        // Verificar se o professor é o criador
        if (!formularioOriginal.getCriador().getId().equals(usuario.getId())) {
            redirectAttributes.addFlashAttribute("erro", "Você não tem permissão para duplicar este formulário");
            return "redirect:/professor/formularios";
        }

        // Criar cópia do formulário
        Formulario novoFormulario = new Formulario();
        novoFormulario.setTitulo(formularioOriginal.getTitulo() + " (Cópia)");
        novoFormulario.setDescricao(formularioOriginal.getDescricao());
        novoFormulario.setIsAnonimo(formularioOriginal.getIsAnonimo());
        novoFormulario.setPermiteEdicao(formularioOriginal.getPermiteEdicao());

        var processos = processoService.listarTodos();
        List<Turma> turmas = turmaService.buscarPorProfessor(usuario);

        model.addAttribute("formulario", novoFormulario);
        model.addAttribute("formularioOriginal", formularioOriginal);
        model.addAttribute("processos", processos);
        model.addAttribute("turmas", turmas);
        model.addAttribute("paginaTitulo", "Duplicar Formulário");

        return "professor/criar-formulario";
    }

    /**
     * Salva formulário (criar ou atualizar)
     * RF07
     */
    @PostMapping("/formularios/salvar")
    public String salvarFormulario(@ModelAttribute Formulario formulario,
                                    @RequestParam(required = false) Integer processoId,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = obterUsuarioLogado(authentication);
            Formulario salvo;

            // Se é um novo formulário
            if (formulario.getId() == null) {
                ProcessoAvaliativo processo = null;
                if (processoId != null) {
                    processo = processoService.buscarPorId(processoId)
                            .orElse(null);
                }
                salvo = formularioService.criar(formulario, processo, null, usuario);
            } else {
                // Se está editando
                Formulario existente = formularioService.buscarPorId(formulario.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

                if (!existente.getCriador().getId().equals(usuario.getId())) {
                    redirectAttributes.addFlashAttribute("erro", "Você não tem permissão para editar este formulário");
                    return "redirect:/professor/formularios";
                }

                salvo = formularioService.atualizar(formulario);
            }

            redirectAttributes.addFlashAttribute("sucesso", "Formulário salvo com sucesso!");
            return "redirect:/professor/formularios/" + salvo.getId() + "/questoes";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar formulário: " + e.getMessage());
            return "redirect:/professor/formularios/novo";
        }
    }

    /**
     * Adiciona questão ao formulário
     * RF08, RF09, RF10
     */
    @PostMapping("/formularios/{id}/questoes/adicionar")
    public String adicionarQuestao(@PathVariable Integer id,
                                    @RequestParam String texto,
                                    @RequestParam String tipo,
                                    @RequestParam(defaultValue = "true") Boolean obrigatoria,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = obterUsuarioLogado(authentication);

            Formulario formulario = formularioService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

            // Verificar se o professor é o criador
            if (!formulario.getCriador().getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute("erro", "Você não tem permissão para adicionar questões");
                return "redirect:/professor/formularios";
            }

            // Criar questão usando o service
            questaoService.criar(id, texto, TipoQuestao.valueOf(tipo), obrigatoria);

            redirectAttributes.addFlashAttribute("sucesso", "Questão adicionada com sucesso!");
            return "redirect:/professor/formularios/" + id + "/questoes";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao adicionar questão: " + e.getMessage());
            return "redirect:/professor/formularios/" + id + "/questoes";
        }
    }

    /**
     * Adiciona opção (alternativa) a uma questão
     * RF08
     */
    @PostMapping("/questoes/{id}/opcoes/adicionar")
    public String adicionarOpcao(@PathVariable Integer id,
                                  @RequestParam String textoAlternativa,
                                  @RequestParam(required = false) java.math.BigDecimal peso,
                                  @RequestParam(defaultValue = "false") Boolean isCorreta,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = obterUsuarioLogado(authentication);

            Questao questao = questaoService.buscarPorId(id);
            if (questao == null) {
                throw new IllegalArgumentException("Questão não encontrada");
            }

            // Verificar se o professor é o criador do formulário
            if (!questao.getFormulario().getCriador().getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute("erro", "Você não tem permissão para adicionar opções");
                return "redirect:/professor/formularios";
            }

            // Criar alternativa usando o service
            alternativaService.criar(id, textoAlternativa, peso, isCorreta);

            redirectAttributes.addFlashAttribute("sucesso", "Opção adicionada com sucesso!");
            return "redirect:/professor/formularios/" + questao.getFormulario().getId() + "/questoes";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao adicionar opção: " + e.getMessage());
            return "redirect:/professor/formularios";
        }
    }

    // ==================== TURMAS ====================

    /**
     * Lista turmas do professor
     */
    @GetMapping("/turmas")
    public String listarTurmas(Authentication authentication, Model model) {
        Usuario usuario = obterUsuarioLogado(authentication);

        List<Turma> turmas = turmaService.buscarPorProfessor(usuario);

        model.addAttribute("turmas", turmas);
        model.addAttribute("paginaTitulo", "Minhas Turmas");

        return "professor/turmas/lista";
    }

    /**
     * Exibe detalhes de uma turma
     * RF05, RF06
     */
    @GetMapping("/turmas/{id}")
    public String detalhesTurma(@PathVariable Integer id,
                                Authentication authentication,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Usuario usuario = obterUsuarioLogado(authentication);

        Turma turma = turmaService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));

        // Verificar se o professor está vinculado à turma
        if (!turma.getProfessores().contains(usuario)) {
            redirectAttributes.addFlashAttribute("erro", "Você não tem acesso a esta turma");
            return "redirect:/professor/turmas";
        }

        model.addAttribute("turma", turma);
        model.addAttribute("alunos", turma.getAlunos());
        model.addAttribute("professores", turma.getProfessores());
        model.addAttribute("paginaTitulo", "Turma - " + turma.getUc().getNome());

        return "professor/turma";
    }

    // ==================== RELATÓRIOS ====================

    /**
     * Lista relatórios disponíveis
     */
    @GetMapping("/relatorios")
    public String listarRelatorios(Authentication authentication, Model model) {
        Usuario usuario = obterUsuarioLogado(authentication);

        // Buscar formulários que têm submissões
        List<Formulario> formulariosComRespostas = formularioService.listarPorCriador(usuario);

        model.addAttribute("formularios", formulariosComRespostas);
        model.addAttribute("paginaTitulo", "Relatórios");

        return "professor/relatorios/lista";
    }

    /**
     * Exibe relatório detalhado de um formulário
     */
    @GetMapping("/relatorios/{formularioId}")
    public String relatorioDetalhado(@PathVariable Integer formularioId,
                                     @RequestParam(required = false) Integer turmaId,
                                     Authentication authentication,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        Usuario usuario = obterUsuarioLogado(authentication);

        Formulario formulario = formularioService.buscarPorId(formularioId)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        // Verificar se o professor é o criador
        if (!formulario.getCriador().getId().equals(usuario.getId())) {
            redirectAttributes.addFlashAttribute("erro", "Você não tem permissão para ver este relatório");
            return "redirect:/professor/relatorios";
        }

        // Contar submissões
        Long totalRespostas = formularioService.contarSubmissoes(formularioId);

        model.addAttribute("formulario", formulario);
        model.addAttribute("totalRespostas", totalRespostas);
        model.addAttribute("turmaId", turmaId);
        model.addAttribute("paginaTitulo", "Relatório: " + formulario.getTitulo());

        return "professor/relatorios/detalhado";
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
