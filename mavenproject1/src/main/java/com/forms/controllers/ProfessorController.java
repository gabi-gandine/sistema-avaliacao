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
@RequestMapping("/professor")
@PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADOR', 'ADMINISTRADOR')")
public class ProfessorController {

    @Autowired
    private CustomUserDetailsService userDetailsService;
    
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
        
        // TODO: Lógica para buscar as turmas onde este usuário é professor 
        // E buscar as avaliações que ele criou.

        model.addAttribute("usuario", professor);
        model.addAttribute("perfil", professor.getPerfil().getNome());
        model.addAttribute("paginaTitulo", "Dashboard do Professor");
        model.addAttribute("turmasComoProfessor", professor.getTurmasComoProfessor());

        return "professor/dashboard";
    }

}