package com.forms.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException; 

import com.forms.models.Turma;
import com.forms.models.Usuario;
import com.forms.repository.TurmaRepository;
import com.forms.security.CustomUserDetailsService;

@Controller
@RequestMapping("/turma")
@PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADOR', 'ADMINISTRADOR')")
public class TurmaController {

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @GetMapping("/{id}")
    public String singlePathVariable(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer id, Model model) {

        Turma turma = this.turmaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada com ID: " + id));

        String email = userDetails.getUsername();
        Usuario professor = userDetailsService.loadUsuarioByEmail(email);

        model.addAttribute("turma", turma);

        model.addAttribute("usuario", professor);
        model.addAttribute("perfil", professor.getPerfil().getNome());
        model.addAttribute("turmaId", turma.getId());
        
        model.addAttribute("paginaTitulo", "Turma - " + turma.getUc().getNome());
        
        model.addAttribute("ano", turma.getAno());
        model.addAttribute("semestre", turma.getSemestre());
        model.addAttribute("alunos", turma.getAlunos());

        return "professor/turma";
    }
}