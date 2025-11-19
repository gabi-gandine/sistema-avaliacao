package com.forms.controller.web;

import com.forms.models.Formulario;
import com.forms.models.Usuario;
import com.forms.service.FormularioService;
import com.forms.service.SubmissaoService;
import com.forms.service.TurmaService;
import com.forms.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller MVC para interface do Aluno
 *
 * RF12 - Aluno acessa apenas avaliações de suas turmas
 * RF13 - Cada aluno responde apenas uma vez cada formulário
 * RF14 - Formulários anônimos registram resposta sem vincular identidade
 * RF15 - Bloqueio de acesso a respostas de outros alunos
 */
@Controller
@RequestMapping("/aluno")
public class AlunoWebController {

    @Autowired
    private FormularioService formularioService;

    @Autowired
    private SubmissaoService submissaoService;

    @Autowired
    private TurmaService turmaService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Dashboard do aluno
     * RF12 - Exibe avaliações pendentes das turmas do aluno
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Usuario usuario = obterUsuarioLogado(authentication);

        // RF12: Buscar formulários das turmas do aluno
        List<Formulario> formulariosDisponiveis = formularioService.listarDisponiveisParaAluno(usuario.getId());

        model.addAttribute("usuario", usuario);
        model.addAttribute("avaliacoesPendentes", formulariosDisponiveis);
        model.addAttribute("paginaTitulo", "Dashboard - Aluno");

        return "aluno/dashboard";
    }

    /**
     * Lista formulários disponíveis para o aluno
     * RF12 - Apenas formulários das turmas onde está matriculado
     * RF13 - Marca formulários já respondidos
     */
    @GetMapping("/formularios")
    public String listarFormularios(Authentication authentication, Model model) {
        Usuario usuario = obterUsuarioLogado(authentication);

        // RF12: Formulários disponíveis para as turmas do aluno
        List<Formulario> formularios = formularioService.listarDisponiveisParaAluno(usuario.getId());

        // RF06: Turmas do aluno
        model.addAttribute("formularios", formularios);
        model.addAttribute("minhasTurmas", usuario.getTurmasComoAluno());
        model.addAttribute("paginaTitulo", "Formulários Disponíveis");

        return "aluno/formularios-disponiveis";
    }

    /**
     * Exibe formulário para o aluno responder
     * RF08, RF09, RF10 - Questões de múltipla escolha e abertas
     * RF11, RF14 - Respeita configuração de anonimato
     */
    @GetMapping("/formularios/{id}/responder")
    public String responderFormulario(@PathVariable Integer id,
                                     Authentication authentication,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        Usuario usuario = obterUsuarioLogado(authentication);

        // Buscar formulário
        Formulario formulario = formularioService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        // RF12: Verificar se aluno tem acesso (está em turma vinculada)
        if (!formularioService.alunoTemAcesso(usuario.getId(), id)) {
            redirectAttributes.addFlashAttribute("erro", "Você não tem acesso a este formulário");
            return "redirect:/aluno/formularios";
        }

        // RF13: Verificar se já respondeu
        if (submissaoService.jaRespondeu(usuario.getId(), id)) {
            redirectAttributes.addFlashAttribute("aviso", "Você já respondeu este formulário");
            return "redirect:/aluno/minhas-respostas";
        }

        // RF11: Verificar se formulário está ativo
        if (!formulario.isAtivo()) {
            redirectAttributes.addFlashAttribute("erro", "Este formulário não está mais disponível");
            return "redirect:/aluno/formularios";
        }

        model.addAttribute("formulario", formulario);
        model.addAttribute("questoes", formulario.getQuestoes());
        model.addAttribute("isAnonimo", formulario.getIsAnonimo());
        model.addAttribute("paginaTitulo", "Responder: " + formulario.getTitulo());

        return "aluno/responder-formulario";
    }

    /**
     * Processa submissão de respostas do formulário
     * RF13 - Garante resposta única
     * RF14 - Registra resposta respeitando anonimato
     * RF03 - Sempre registra quem respondeu via SubmissaoControle
     */
    @PostMapping("/formularios/{id}/responder")
    public String submeterRespostas(@PathVariable Integer id,
                                   Authentication authentication,
                                   @RequestParam(required = false) String[] respostas,
                                   RedirectAttributes redirectAttributes) {
        Usuario usuario = obterUsuarioLogado(authentication);

        try {
            // RF13: Verificar se já respondeu
            if (submissaoService.jaRespondeu(usuario.getId(), id)) {
                redirectAttributes.addFlashAttribute("erro", "Você já respondeu este formulário");
                return "redirect:/aluno/formularios";
            }

            // RF03, RF14: Submeter respostas (sistema registra quem respondeu)
            submissaoService.submeterRespostas(usuario.getId(), id, respostas);

            redirectAttributes.addFlashAttribute("sucesso", "Respostas enviadas com sucesso!");
            return "redirect:/aluno/minhas-respostas";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao enviar respostas: " + e.getMessage());
            return "redirect:/aluno/formularios/" + id + "/responder";
        }
    }

    /**
     * Lista respostas já submetidas pelo aluno
     * RF13 - Permite visualizar formulários respondidos
     * RF14, RF15 - Exibe apenas respostas próprias
     */
    @GetMapping("/minhas-respostas")
    public String minhasRespostas(Authentication authentication, Model model) {
        Usuario usuario = obterUsuarioLogado(authentication);

        // RF15: Apenas respostas do próprio aluno
        var respostas = submissaoService.listarPorAluno(usuario.getId());

        model.addAttribute("respostasAgrupadas", respostas);
        model.addAttribute("paginaTitulo", "Minhas Respostas");

        return "aluno/minhas-respostas";
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
