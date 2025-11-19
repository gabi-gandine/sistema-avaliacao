package com.forms.controllers;

import com.forms.models.Turma;
import com.forms.models.Usuario;
import com.forms.models.Avaliacao;
import com.forms.repository.AvaliacaoRepository;
import com.forms.security.CustomUserDetailsService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/aluno")
@PreAuthorize("hasRole('ALUNO')") 
public class AlunoController {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;
    
    // @Autowired 
    // private AvaliacaoService avaliacaoService; 

    /**
     * Tela inicial/Dashboard do Aluno.
     * RF12: O aluno deve ter acesso apenas às avaliações das turmas em que está matriculado.
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        Usuario aluno = userDetailsService.loadUsuarioByEmail(email);

        List<Avaliacao> avaliacoesPendentes = new ArrayList<Avaliacao>();

        for(Turma turma : aluno.getTurmasComoAluno()) {
            LocalDateTime agora = LocalDateTime.now();
            avaliacoesPendentes.addAll(avaliacaoRepository.findAvaliacoesAtivasPorTurma(turma.getId(), agora));
        }
        
        // TODO: Implementar a lógica para buscar as avaliações ativas para este aluno (RF12)

        model.addAttribute("usuario", aluno);
        model.addAttribute("avaliacoesPendentes", avaliacoesPendentes);
        model.addAttribute("paginaTitulo", "Dashboard do Aluno");

        return "aluno/dashboard"; 
    }

}
