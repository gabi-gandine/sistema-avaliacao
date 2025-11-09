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
@RequestMapping("/coordenador")
@PreAuthorize("hasAnyRole('COORDENADOR', 'ADMINISTRADOR')")
public class CoordenadorController {

    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    // @Autowired
    // private CursoService cursoService;
    // @Autowired
    // private RelatorioService relatorioService; 

    /**
     * Tela inicial/Dashboard do Coordenador.
     * Foco em gestão de contexto avaliativo e relatórios consolidados (RF04, RF05, RF16).
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
    
    // Próximos métodos a implementar:
    // 1. Gerenciar Cursos e UCs: @GetMapping("/gestao/cursos") (RF04)
    // 2. Gerenciar Processos Avaliativos: @GetMapping("/gestao/processo") (RF05)
    // 3. Ver Relatório de Curso/Professor: @GetMapping("/relatorio/{cursoId}") (RF16)
}