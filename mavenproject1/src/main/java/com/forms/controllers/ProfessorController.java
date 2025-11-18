package com.forms.controllers;

import com.forms.models.Avaliacao;
import com.forms.models.OpcaoResposta;
import com.forms.models.Questao;
import com.forms.models.TipoQuestao;
import com.forms.models.Turma;
import com.forms.models.Usuario;
import com.forms.repository.AvaliacaoRepository;
import com.forms.repository.OpcaoRespostaRepository;
import com.forms.repository.QuestaoRepository;
import com.forms.repository.TurmaRepository;
import com.forms.security.CustomUserDetailsService;

import jakarta.validation.Valid;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/professor")
@PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADOR', 'ADMINISTRADOR')")
public class ProfessorController {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private OpcaoRespostaRepository opcaoRepository;
    
    // @Autowired
    // private TurmaService turmaService;
    
    /**
     * Tela inicial/Dashboard do Professor.
     * Deverá listar as turmas que o professor leciona (RF05) e as avaliações criadas/em andamento.
     */

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        Usuario professor = userDetailsService.loadUsuarioByEmail(email);

        List<Avaliacao> avaliacoes = avaliacaoRepository.findByCriador(professor);

        model.addAttribute("usuario", professor);
        model.addAttribute("perfil", professor.getPerfil().getNome());
        model.addAttribute("paginaTitulo", "Dashboard do Professor");
        
        model.addAttribute("avaliacoes", avaliacoes); 

        model.addAttribute("turmasComoProfessor", professor.getTurmasComoProfessor());

        return "professor/dashboard";
    }

    @GetMapping("/avaliacao/criar")
    public String formCriarAvaliacao(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        Usuario professor = userDetailsService.loadUsuarioByEmail(email);

        // Busca apenas as turmas desse professor
        List<Turma> minhasTurmas = turmaRepository.findByProfessor(professor);

        model.addAttribute("avaliacao", new Avaliacao());
        model.addAttribute("turmas", minhasTurmas); 
        model.addAttribute("paginaTitulo", "Nova Avaliação");
        
        return "professor/formAvaliacao";
    }

    @PostMapping("/avaliacao/salvar")
    public String salvarAvaliacao(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @ModelAttribute Avaliacao avaliacao,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "professor/formAvaliacao";
        }

        // Define o criador como o usuário logado (Professor)
        String email = userDetails.getUsername();
        Usuario professor = userDetailsService.loadUsuarioByEmail(email);
        avaliacao.setCriador(professor);

        avaliacaoRepository.save(avaliacao);
        
        redirectAttributes.addFlashAttribute("success", "Avaliação criada! Agora adicione as questões.");
        
        // Redireciona para a tela de gerenciar questões desta avaliação específica
        return "redirect:/professor/avaliacao/" + avaliacao.getId() + "/questoes";
    }

    @GetMapping("/avaliacao/{id}/questoes")
    public String gerenciarQuestoes(@PathVariable Integer id, Model model) {
        Avaliacao avaliacao = avaliacaoRepository.findById(id).get();

        model.addAttribute("avaliacao", avaliacao);
        model.addAttribute("questoes", questaoRepository.findByAvaliacaoOrderByOrdemAsc(avaliacao));
        
        model.addAttribute("novaQuestao", new Questao());
        
        return "professor/gerenciarQuestoes";
    }

    @PostMapping("/avaliacao/{id}/adicionar-questao")
    public String adicionarQuestao(@PathVariable Integer id, 
                                @ModelAttribute Questao questao,
                                RedirectAttributes redirectAttributes) {
        
        Avaliacao avaliacao = avaliacaoRepository.findById(id).get();
        Questao novaQuestao = new Questao();
        
        novaQuestao.setTexto(questao.getTexto());
        novaQuestao.setTipo(questao.getTipo());
        novaQuestao.setAvaliacao(avaliacao);
        
        novaQuestao.setOrdem(avaliacao.getQuestoes().size() + 1);
        
        questaoRepository.save(novaQuestao);
        
        return "redirect:/professor/avaliacao/" + id + "/questoes";
    }

    @PostMapping("/avaliacao/questao/{id}/adicionar-opcao")
    public String adicionarOpcao(@PathVariable Integer id, 
                                @ModelAttribute OpcaoResposta opcao,
                                RedirectAttributes redirectAttributes) {
        
        Questao questao = questaoRepository.findById(id).get();

        OpcaoResposta op = new OpcaoResposta();

        op.setIsCorreta(opcao.getIsCorreta());
        op.setTexto(opcao.getTexto());
        op.setQuestao(questao);
        op.setOrdem(questao.getOpcoes().size() + 1);
        
        opcaoRepository.save(op);
        
        return "redirect:/professor/avaliacao/questao/" + id;
    }

    @GetMapping("/avaliacao/questao/{questaoId}")
    public String gerenciarOpcoes(@PathVariable Integer questaoId, Model model) {
        Questao q = questaoRepository.findById(questaoId).get();
        boolean questaoCorreta = false;

        if(q.getTipo() == TipoQuestao.MULTIPLA_ESCOLHA_UNICA) {
            if(opcaoRepository.findByQuestaoAndIsCorreta(q, true).isPresent()) {
                questaoCorreta = true;
            }
        }

        model.addAttribute("questao", q);

        model.addAttribute("opcoes", opcaoRepository.findByQuestao(q));
        
        model.addAttribute("opcao", new OpcaoResposta());
        model.addAttribute("opcaoCorreta", questaoCorreta);
        
        return "professor/gerenciarOpcoes";
    }
}