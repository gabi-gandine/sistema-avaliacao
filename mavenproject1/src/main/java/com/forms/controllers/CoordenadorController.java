package com.forms.controllers;

import com.forms.models.Usuario;
import com.forms.models.Curso;
import com.forms.models.Perfil;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        Optional<Perfil> perfilProfessor = perfilService.buscarPorNome("PROFESSOR");
        Optional<Perfil> perfilAluno = perfilService.buscarPorNome("ALUNO");

        if (perfilProfessor.isPresent()) {
            model.addAttribute("professores", usuarioService.listarPorPerfil(perfilProfessor.get()));
        } else {
            System.out.println("ERRO: Perfil PROFESSOR não encontrado no banco.");
        }

        if (perfilAluno.isPresent()) {
            model.addAttribute("alunos", usuarioService.listarPorPerfil(perfilAluno.get()));
        } else {
            System.out.println("ERRO: Perfil ALUNO não encontrado no banco.");
        }
        
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
    // CRUD TURMA
    // =========================================================================
    
    @PostMapping("/turma/salvar")
    public String salvarTurma(
            @Valid @ModelAttribute("novaTurma") Turma turmaDadosBasicos, 
            BindingResult result,
            RedirectAttributes redirectAttributes,
            @RequestParam(value = "alunoIds", required = false) List<Integer> alunoIds, // required=false para evitar erro se vazio
            @RequestParam("professorId") Integer professorId,
            @RequestParam("ucId") Integer ucId) {
        
        if (result.hasErrors()) {
             redirectAttributes.addFlashAttribute("error", "Erro nos dados da turma: " + result.getFieldError().getDefaultMessage());
             return "redirect:/coordenador/gestao/cursos";
        }
        
        try {
            // 1. Buscas as entidades fixas (Professor e UC)
            Usuario professor = usuarioService.buscarPorId(professorId)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado."));
            
            UnidadeCurricular uc = ucService.buscarPorId(ucId)
                .orElseThrow(() -> new IllegalArgumentException("Unidade Curricular não encontrada."));

            // 2. Cria a INSTÂNCIA da Turma APENAS UMA VEZ
            Turma novaTurma = new Turma();
            
            // Se estiver editando uma turma existente (id não nulo), busque-a do banco
            if (turmaDadosBasicos.getId() != null) {
                novaTurma = turmaService.buscarPorId(turmaDadosBasicos.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada para edição."));
            }

            // 3. Define os dados básicos
            novaTurma.setAno(turmaDadosBasicos.getAno());
            novaTurma.setSemestre(turmaDadosBasicos.getSemestre());
            novaTurma.setProfessor(professor);
            novaTurma.setUc(uc);

            // 4. Gerencia a lista de alunos
            if (alunoIds != null && !alunoIds.isEmpty()) {
                Set<Usuario> alunosSet = new HashSet<>(); 
                
                for (Integer alunoId : alunoIds) {
                    Usuario aluno = usuarioService.buscarPorId(alunoId).orElse(null);
                    if (aluno != null) {
                        alunosSet.add(aluno);
                    }
                }
                novaTurma.setAlunos(alunosSet);
            }
            
            // 5. Salva UMA VEZ a turma contendo VÁRIOS alunos
            turmaService.salvar(novaTurma);
            
            int qtdAlunos = (novaTurma.getAlunos() != null) ? novaTurma.getAlunos().size() : 0;
            redirectAttributes.addFlashAttribute("success", "Turma salva com sucesso com " + qtdAlunos + " aluno(s) matriculado(s)!");
            
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
            // Erro de negócio (ex: id não existe)
            redirectAttributes.addFlashAttribute("error", "Erro: " + e.getMessage());
        } catch (Exception e) {
            // Erro de banco de dados (ex: existe uma avaliação usando esta turma)
            redirectAttributes.addFlashAttribute("error", "Não foi possível deletar. Verifique se já existe uma Avaliação ou registro vinculado a esta turma.");
        }
        return "redirect:/coordenador/gestao/cursos";
    }

    // Próximos métodos a implementar:
    // 2. Gerenciar Processos Avaliativos: @GetMapping("/gestao/processo") (RF05)
    // 3. Ver Relatório de Curso/Professor: @GetMapping("/relatorio/{cursoId}") (RF16)
}