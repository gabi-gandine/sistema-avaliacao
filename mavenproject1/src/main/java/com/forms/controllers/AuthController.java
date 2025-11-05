package com.forms.controllers;

import com.forms.models.Usuario;
import com.forms.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsável por endpoints de autenticação e páginas principais
 */
@Controller
public class AuthController {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Usuario usuario = userDetailsService.loadUsuarioByEmail(email);

            model.addAttribute("usuario", usuario);
            model.addAttribute("perfil", usuario.getPerfil().getNome());

            switch (usuario.getPerfil().getNome()) {
                case "ALUNO":
                    return "redirect:/aluno/dashboard";
                case "PROFESSOR":
                    return "redirect:/professor/dashboard";
                case "COORDENADOR":
                    return "redirect:/coordenador/dashboard";
                case "ADMINISTRADOR":
                    return "redirect:/admin/dashboard";
                default:
                    return "home";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/perfil")
    public String perfil(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            String email = userDetails.getUsername();
            Usuario usuario = userDetailsService.loadUsuarioByEmail(email);
            model.addAttribute("usuario", usuario);
            return "perfil";
        }
        return "redirect:/login";
    }
}
