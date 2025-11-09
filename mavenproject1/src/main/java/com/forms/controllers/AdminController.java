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
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminController {

    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    // @Autowired
    // private UsuarioService usuarioService;
    // @Autowired
    // private AuditoriaService auditoriaService;

    /**
     * Tela inicial/Dashboard do Administrador.
     * Foco em gestão de sistema e acesso a dados brutos (RF20, RNF04).
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        Usuario admin = userDetailsService.loadUsuarioByEmail(email);
        
        // TODO: Lógica para exibir estatísticas gerais do sistema (contagem de usuários, avaliações, etc.)

        model.addAttribute("usuario", admin);
        model.addAttribute("perfil", admin.getPerfil().getNome());
        model.addAttribute("paginaTitulo", "Dashboard do Administrador");

        return "admin/dashboard"; 
    }
    
    // Próximos métodos a implementar:
    // 1. Gestão de Usuários: @GetMapping("/gestao/usuarios")
    // 2. Gestão de Perfis: @GetMapping("/gestao/perfis")
    // 3. Auditoria/Logs: @GetMapping("/auditoria") (RNF04)
    // 4. Acesso a Dados Brutos: @GetMapping("/dados-brutos") (RF20)
}
