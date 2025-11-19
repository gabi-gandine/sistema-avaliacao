package com.forms.controller.web;

import com.forms.models.Perfil;
import com.forms.models.Usuario;
import com.forms.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller MVC para interface do Administrador
 *
 * RF01 - Gestão de usuários
 * RF20 - Exportação de dados e auditoria
 * RNF04 - Logs de auditoria
 */
@Controller
@RequestMapping("/admin")
public class AdminWebController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PerfilService perfilService;

    @Autowired
    private LogAuditoriaService logAuditoriaService;

    @Autowired
    private FormularioService formularioService;

    @Autowired
    private TurmaService turmaService;

    /**
     * Dashboard do administrador
     * Exibe estatísticas do sistema
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Usuario usuario = obterUsuarioLogado(authentication);

        // Estatísticas gerais
        Long totalUsuarios = (long) usuarioService.listarTodos().size();
        Long totalFormularios = (long) formularioService.listarTodos().size();
        Long totalTurmas = (long) turmaService.listarTodas().size();
        Long totalPerfis = (long) perfilService.listarTodos().size();

        model.addAttribute("usuario", usuario);
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalFormularios", totalFormularios);
        model.addAttribute("totalTurmas", totalTurmas);
        model.addAttribute("totalPerfis", totalPerfis);
        model.addAttribute("paginaTitulo", "Dashboard - Administrador");

        return "admin/dashboard";
    }

    // ==================== GESTÃO DE USUÁRIOS ====================

    /**
     * Lista todos os usuários
     * RF01
     */
    @GetMapping("/usuarios")
    public String listarUsuarios(@RequestParam(required = false) Integer perfilId, Model model) {
        List<Usuario> usuarios;

        if (perfilId != null) {
            Perfil perfil = perfilService.buscarPorId(perfilId)
                    .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado"));
            usuarios = usuarioService.listarPorPerfil(perfil);
        } else {
            usuarios = usuarioService.listarTodos();
        }

        List<Perfil> perfis = perfilService.listarTodos();

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("perfis", perfis);
        model.addAttribute("perfilId", perfilId);
        model.addAttribute("paginaTitulo", "Gestão de Usuários");

        return "admin/usuarios/lista";
    }

    /**
     * Exibe formulário para criar novo usuário
     * RF01
     */
    @GetMapping("/usuarios/novo")
    public String criarUsuario(Model model) {
        List<Perfil> perfis = perfilService.listarTodos();

        model.addAttribute("usuario", new Usuario());
        model.addAttribute("perfis", perfis);
        model.addAttribute("acao", "criar");
        model.addAttribute("paginaTitulo", "Novo Usuário");

        return "admin/usuarios/form";
    }

    /**
     * Exibe formulário para editar usuário
     * RF01
     */
    @GetMapping("/usuarios/{id}/editar")
    public String editarUsuario(@PathVariable Integer id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        List<Perfil> perfis = perfilService.listarTodos();

        model.addAttribute("usuario", usuario);
        model.addAttribute("perfis", perfis);
        model.addAttribute("acao", "editar");
        model.addAttribute("paginaTitulo", "Editar Usuário");

        return "admin/usuarios/form";
    }

    // ==================== GESTÃO DE PERFIS ====================

    /**
     * Gerenciar perfis do sistema
     * RF01
     */
    @GetMapping("/perfis")
    public String gerenciarPerfis(Model model) {
        List<Perfil> perfis = perfilService.listarTodos();

        // Contar usuários por perfil
        model.addAttribute("perfis", perfis);
        model.addAttribute("paginaTitulo", "Gestão de Perfis");

        return "admin/perfis/gerenciar";
    }

    // ==================== AUDITORIA E LOGS ====================

    /**
     * Visualizar logs de auditoria
     * RF20, RNF04
     */
    @GetMapping("/auditoria")
    public String logsAuditoria(@RequestParam(required = false) Integer usuarioId,
                               @RequestParam(required = false) String acao,
                               Model model) {
        // Buscar logs (com filtros se fornecidos)
        var logs = logAuditoriaService.buscarRecentes();

        List<Usuario> usuarios = usuarioService.listarTodos();

        model.addAttribute("logs", logs);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("usuarioId", usuarioId);
        model.addAttribute("acao", acao);
        model.addAttribute("paginaTitulo", "Logs de Auditoria");

        return "admin/auditoria/logs";
    }

    // ==================== EXPORTAÇÃO DE DADOS ====================

    /**
     * Interface de exportação de dados
     * RF20
     */
    @GetMapping("/exportar")
    public String exportarDados(Model model) {
        model.addAttribute("paginaTitulo", "Exportar Dados do Sistema");

        return "admin/sistema/exportar";
    }

    /**
     * Helper method para obter usuário logado
     */
    private Usuario obterUsuarioLogado(Authentication authentication) {
        String email = authentication.getName();
        return usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado"));
    }
}
