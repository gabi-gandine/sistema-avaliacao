package com.forms.controllers;

import com.forms.models.Usuario;
import com.forms.security.CustomUserDetailsService;
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
        
        // TODO: Implementar a lógica para buscar as avaliações ativas para este aluno (RF12)

        model.addAttribute("usuario", aluno);
        // model.addAttribute("avaliacoesPendentes", avaliacoes);
        model.addAttribute("paginaTitulo", "Dashboard do Aluno");

        return "turma/${turma.id}"; 
    }

}
