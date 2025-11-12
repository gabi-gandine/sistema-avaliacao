package com.forms.controllers;

import com.forms.models.Usuario;
import com.forms.models.Curso;
import com.forms.models.UnidadeCurricular;
import com.forms.models.Turma;
import com.forms.security.CustomUserDetailsService;
import com.forms.service.CursoService;
import com.forms.service.UnidadeCurricularService;
import com.forms.service.TurmaService;
import com.forms.service.UsuarioService;
import com.forms.service.PerfilService; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/coordenador")
@PreAuthorize("hasAnyRole('COORDENADOR', 'ADMINISTRADOR')")
public class CoordenadorController {

    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private CursoService cursoService;
    
    @Autowired
    private UnidadeCurricularService ucService;
    
    @Autowired
    private TurmaService turmaService;

    @Autowired 
    private UsuarioService usuarioService;

    @Autowired 
    private PerfilService perfilService;
    
    // @Autowired
    // private RelatorioService relatorioService; 

    /**
     * Tela inicial/Dashboard do Coordenador.
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        Usuario coordenador = userDetailsService.loadUsuarioByEmail(email);
        
        // TODO: Lógica para buscar Cursos sob sua coordenação (RF04) e avaliações em andamento.

        model.addAttribute("usuario", coordenador);
        model.addAttribute("perfil", coordenador.getPerfil().getNome());
        model.addAttribute("paginaTitulo", "Dashboard do Coordenador");

        return "coordenador/dashboard"; 
    }
    
    /**
     * 1. Gerenciar Cursos, UCs e Turmas (RF04, RF05).
     */
    @GetMapping("/gestao/cursos")
    public String gerenciarCursos(
            @RequestParam(value = "cursoId", required = false) Integer cursoId,
            @RequestParam(value = "ucId", required = false) Integer ucId,
            @RequestParam(value = "turmaId", required = false) Integer turmaId,
            Model model) {
        
        // Listar todos os dados para visualização
        model.addAttribute("cursos", cursoService.listarTodos());
        model.addAttribute("ucs", ucService.listarTodos());
        model.addAttribute("turmas", turmaService.listarTodas());
        
        // Buscar usuários com perfil PROFESSOR e ALUNO
        Optional<Usuario> professor = usuarioService.buscarPorPerfilNome("PROFESSOR");
        Optional<Usuario> aluno = usuarioService.buscarPorPerfilNome("ALUNO");
        
        // Adicionar listas de usuários para seleção na Turma
        professor.ifPresent(p -> model.addAttribute("professores", usuarioService.listarPorPerfil(p.getPerfil())));
        aluno.ifPresent(a -> model.addAttribute("alunos", usuarioService.listarPorPerfil(a.getPerfil())));
        
        // Formulários vazios para criação
        model.addAttribute("novoCurso", new Curso());
        model.addAttribute("novaUC", new UnidadeCurricular());
        model.addAttribute("novaTurma", new Turma());
        
        model.addAttribute("paginaTitulo", "Gestão de Cursos e Turmas");
        return "coordenador/gestaoCursos"; 
    }

    // =========================================================================
    // CRUD CURSO (RF04)
    // =========================================================================

    @PostMapping("/curso/salvar")
    public String salvarCurso(
            @Valid @ModelAttribute("novoCurso") Curso curso,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
             redirectAttributes.addFlashAttribute("error", "Erro ao salvar Curso: " + result.getFieldError().getDefaultMessage());
             return "redirect:/coordenador/gestao/cursos";
        }
        
        try {
            cursoService.salvar(curso);
            redirectAttributes.addFlashAttribute("success", "Curso salvo com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Erro: " + e.getMessage());
        }

        return "redirect:/coordenador/gestao/cursos";
    }

    @GetMapping("/curso/deletar/{id}")
    public String deletarCurso(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            cursoService.deletar(id);
            redirectAttributes.addFlashAttribute("success", "Curso deletado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao deletar Curso: " + e.getMessage());
        }
        return "redirect:/coordenador/gestao/cursos";
    }

    // =========================================================================
    // CRUD UNIDADE CURRICULAR (RF04)
    // =========================================================================

    @PostMapping("/uc/salvar")
    public String salvarUC(
            @Valid @ModelAttribute("novaUC") UnidadeCurricular uc,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
             redirectAttributes.addFlashAttribute("error", "Erro ao salvar UC: " + result.getFieldError().getDefaultMessage());
             return "redirect:/coordenador/gestao/cursos";
        }
        
        try {
            ucService.salvar(uc);
            redirectAttributes.addFlashAttribute("success", "Unidade Curricular salva com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Erro: " + e.getMessage());
        }

        return "redirect:/coordenador/gestao/cursos";
    }

    @GetMapping("/uc/deletar/{id}")
    public String deletarUC(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            ucService.deletar(id);
            redirectAttributes.addFlashAttribute("success", "Unidade Curricular deletada com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao deletar UC: " + e.getMessage());
        }
        return "redirect:/coordenador/gestao/cursos";
    }

    // =========================================================================
    // CRUD TURMA (RF05, RF06)
    // =========================================================================
    
    @PostMapping("/turma/salvar")
    public String salvarTurma(
            @Valid @ModelAttribute("novaTurma") Turma turma,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            @RequestParam("alunoId") Integer alunoId,
            @RequestParam("professorId") Integer professorId,
            @RequestParam("ucId") Integer ucId) {
        
        if (result.hasErrors()) {
             redirectAttributes.addFlashAttribute("error", "Erro ao salvar Turma: " + result.getFieldError().getDefaultMessage());
             return "redirect:/coordenador/gestao/cursos";
        }
        
        try {
            turma.setAluno(usuarioService.buscarPorId(alunoId).orElse(null));
            turma.setProfessor(usuarioService.buscarPorId(professorId).orElseThrow(() -> new IllegalArgumentException("Professor não encontrado.")));
            turma.setUc(ucService.buscarPorId(ucId).orElseThrow(() -> new IllegalArgumentException("Unidade Curricular não encontrada.")));
            
            turmaService.salvar(turma);
            redirectAttributes.addFlashAttribute("success", "Turma salva com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Erro: " + e.getMessage());
        }

        return "redirect:/coordenador/gestao/cursos";
    }

    @GetMapping("/turma/deletar/{id}")
    public String deletarTurma(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            turmaService.deletar(id);
            redirectAttributes.addFlashAttribute("success", "Turma deletada com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao deletar Turma: " + e.getMessage());
        }
        return "redirect:/coordenador/gestao/cursos";
    }

    // Próximos métodos a implementar:
    // 2. Gerenciar Processos Avaliativos: @GetMapping("/gestao/processo") (RF05)
    // 3. Ver Relatório de Curso/Professor: @GetMapping("/relatorio/{cursoId}") (RF16)
}