package com.forms.controllers;

import com.forms.models.Usuario;
import com.forms.models.Perfil;
import com.forms.service.UsuarioService;
import com.forms.service.PerfilService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller para gestão de usuários via interface web
 * @author gabriela
 */
@Controller
@RequestMapping("/usuarios")
public class UsuarioWebController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PerfilService perfilService;

    @GetMapping("/cadastro")
    public String showCadastroForm(Model model) {
        List<Perfil> perfis = perfilService.listarTodos();
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("perfis", perfis);
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String createUsuario(
            @Valid @ModelAttribute Usuario usuario,
            BindingResult result,
            @RequestParam("senha") String senha,
            @RequestParam("confirmaSenha") String confirmaSenha,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            List<Perfil> perfis = perfilService.listarTodos();
            model.addAttribute("perfis", perfis);
            model.addAttribute("error", "Por favor, corrija os erros no formulário");
            return "cadastro";
        }

        if (!senha.equals(confirmaSenha)) {
            List<Perfil> perfis = perfilService.listarTodos();
            model.addAttribute("perfis", perfis);
            model.addAttribute("error", "As senhas não coincidem");
            return "cadastro";
        }

        if (senha.length() < 6) {
            List<Perfil> perfis = perfilService.listarTodos();
            model.addAttribute("perfis", perfis);
            model.addAttribute("error", "A senha deve ter no mínimo 6 caracteres");
            return "cadastro";
        }

        try {
            usuarioService.cadastrarUsuario(usuario, senha);
            redirectAttributes.addFlashAttribute("success", "Cadastro realizado com sucesso! Faça login.");
            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            List<Perfil> perfis = perfilService.listarTodos();
            model.addAttribute("perfis", perfis);
            model.addAttribute("error", e.getMessage());
            return "cadastro";
        }
    }
}