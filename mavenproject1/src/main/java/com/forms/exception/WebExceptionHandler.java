package com.forms.exception;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handler de exceções para controllers MVC (@Controller)
 * Retorna páginas HTML ao invés de JSON
 *
 * Note: @ControllerAdvice (não @RestControllerAdvice) é usado para retornar views
 */
@ControllerAdvice(basePackages = "com.forms.controller.web")
public class WebExceptionHandler {

    /**
     * Trata erros de autenticação
     * Redireciona para login com mensagem de erro
     */
    @ExceptionHandler(AuthenticationException.class)
    public String handleAuthenticationException(AuthenticationException ex,
                                               RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erro", "Erro de autenticação: " + ex.getMessage());
        return "redirect:/login";
    }

    /**
     * Trata acesso negado (403)
     * Exibe página de erro 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(AccessDeniedException ex, Model model) {
        model.addAttribute("error", "Acesso Negado");
        model.addAttribute("message", "Você não tem permissão para acessar este recurso");
        model.addAttribute("status", 403);
        return "error/403";
    }

    /**
     * Trata argumentos inválidos
     * Redireciona com mensagem de erro
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex,
                                                RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        return "redirect:/";
    }

    /**
     * Trata estados inválidos
     * Redireciona com mensagem de erro
     */
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException ex,
                                             RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        return "redirect:/";
    }

    /**
     * Handler genérico para exceções não tratadas
     * Exibe página de erro 500
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("error", "Erro Interno");
        model.addAttribute("message", "Ocorreu um erro inesperado: " + ex.getMessage());
        model.addAttribute("status", 500);
        return "error/500";
    }
}
