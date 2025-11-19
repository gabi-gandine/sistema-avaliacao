package com.forms.controller.web;

import com.forms.models.Usuario;
import com.forms.service.PerfilService;
import com.forms.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller MVC para cadastro e gerenciamento de usuários
 * RF01 - Cadastro de perfis
 * RF02 - Autenticação
 */
@Controller
@RequestMapping("/usuarios")
public class UsuarioWebController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PerfilService perfilService;

    /**
     * Exibe formulário de cadastro de usuário
     * RF01 - Permite cadastro dinâmico com seleção de perfil
     */
    @GetMapping("/cadastro")
    public String exibirCadastro(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("perfis", perfilService.listarTodos());
        model.addAttribute("paginaTitulo", "Cadastro de Usuário");
        return "cadastro";
    }

    /**
     * Processa cadastro de novo usuário
     * RF01 - Criar usuário com perfil selecionado
     */
    @PostMapping("/cadastro")
    public String processCadastro(@ModelAttribute Usuario usuario,
                                 @RequestParam String senha,
                                 RedirectAttributes redirectAttributes) {
        try {
            usuarioService.cadastrarUsuario(usuario, senha);
            redirectAttributes.addFlashAttribute("sucesso", "Cadastro realizado com sucesso! Faça login para continuar.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cadastrar usuário: " + e.getMessage());
            return "redirect:/usuarios/cadastro";
        }
    }
}
