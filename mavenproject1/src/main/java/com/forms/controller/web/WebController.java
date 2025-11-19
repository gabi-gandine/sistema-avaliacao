package com.forms.controller.web;

import com.forms.models.Usuario;
import com.forms.service.FormularioService;
import com.forms.service.ProcessoAvaliativoService;
import com.forms.service.SubmissaoService;
import com.forms.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller MVC para páginas públicas e redirecionamento principal
 *
 * Este controller serve a interface web (HTML) e trabalha em conjunto com
 * os REST controllers que servem JSON para chamadas AJAX.
 */
@Controller
public class WebController {

    @Autowired
    private ProcessoAvaliativoService processoService;

    @Autowired
    private FormularioService formularioService;

    @Autowired
    private SubmissaoService submissaoService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Página inicial pública
     * Exibe estatísticas gerais do sistema
     */
    @GetMapping("/")
    public String index(Model model) {
        // Estatísticas para a página inicial
        // TODO: Implementar contadores nos services ou usar repositories diretamente
        model.addAttribute("totalProcessos", 0L);
        model.addAttribute("totalFormularios", 0L);
        model.addAttribute("totalRespostas", 0L);
        model.addAttribute("totalUsuarios", 0L);
        model.addAttribute("paginaTitulo", "Sistema de Avaliação");

        return "index";
    }

    /**
     * Página de login
     * Spring Security já gerencia a autenticação, este método apenas serve a página
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Redirecionamento pós-login baseado no perfil do usuário
     *
     * Redireciona para:
     * - ALUNO → /aluno/dashboard
     * - PROFESSOR → /professor/dashboard
     * - COORDENADOR → /coordenador/dashboard
     * - ADMINISTRADOR → /admin/dashboard
     */
    @GetMapping("/home")
    public String home(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // Redirecionar baseado no perfil/role
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))) {
            return "redirect:/admin/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_COORDENADOR"))) {
            return "redirect:/coordenador/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PROFESSOR"))) {
            return "redirect:/professor/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ALUNO"))) {
            return "redirect:/aluno/dashboard";
        }

        // Fallback: se não tiver perfil reconhecido, volta para login
        return "redirect:/login";
    }
}
