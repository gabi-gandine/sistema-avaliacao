package com.forms.aspect;

import com.forms.models.Usuario;
import com.forms.service.LogAuditoriaService;
import com.forms.service.annotation.Auditavel;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * Aspect AOP para Auditoria Automática (RNF04)
 *
 * Intercepta métodos marcados com @Auditavel e registra automaticamente
 * no log de auditoria.
 *
 * COMO USAR:
 *
 * 1. Marque o método com @Auditavel:
 *    @Auditavel(acao = "CRIAR_FORMULARIO", tipo = "FORMULARIO")
 *    public Formulario criar(...) { ... }
 *
 * 2. O aspect automaticamente:
 *    - Captura o usuário logado
 *    - Executa o método
 *    - Registra SUCESSO se não houver exceção
 *    - Registra FALHA se houver exceção
 *    - Captura IP e User-Agent
 *
 * @author gabriela
 */
@Aspect
@Component
public class AuditoriaAspect {

    @Autowired
    private LogAuditoriaService logService;

    /**
     * Intercepta todos os métodos marcados com @Auditavel
     */
    @Around("@annotation(com.forms.service.annotation.Auditavel)")
    public Object auditar(ProceedingJoinPoint joinPoint) throws Throwable {
        // Obtém a annotation do método
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Auditavel auditavel = method.getAnnotation(Auditavel.class);

        // Obtém o usuário logado (via Spring Security)
        Usuario usuario = getUsuarioLogado();

        // Obtém o HttpServletRequest
        HttpServletRequest request = getHttpServletRequest();

        // Monta a descrição
        String descricao = auditavel.descricao();
        if (descricao.isEmpty()) {
            // Gera descrição automaticamente se não foi fornecida
            descricao = "Ação executada: " + auditavel.acao() +
                       " no método " + method.getName();
        }

        Object result = null;
        boolean sucesso = true;
        String mensagemErro = null;

        try {
            // Executa o método original
            result = joinPoint.proceed();

            // Se chegou aqui, foi sucesso
            logService.registrarSucesso(
                    usuario,
                    auditavel.acao(),
                    descricao,
                    auditavel.tipo(),
                    request
            );

        } catch (Exception e) {
            // Se deu erro, registra falha
            sucesso = false;
            mensagemErro = e.getMessage();

            logService.registrarFalha(
                    usuario,
                    auditavel.acao(),
                    descricao,
                    auditavel.tipo(),
                    request,
                    mensagemErro
            );

            // Re-lança a exceção para não quebrar o fluxo normal
            throw e;
        }

        return result;
    }

    /**
     * Obtém o usuário logado via Spring Security
     */
    private Usuario getUsuarioLogado() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {

                Object principal = authentication.getPrincipal();

                // Se o principal é um UserDetails customizado que contém Usuario
                if (principal instanceof Usuario) {
                    return (Usuario) principal;
                }

                // Se estiver usando UserDetails do Spring Security
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    // Aqui você precisaria buscar o Usuario pelo email/username
                    // Mas para não criar dependência circular, retorna null
                    // O controller pode passar o usuário explicitamente se necessário
                    return null;
                }
            }
        } catch (Exception e) {
            // Se houver erro ao obter usuário, retorna null
            // O log será registrado sem usuário
        }

        return null;
    }

    /**
     * Obtém o HttpServletRequest do contexto atual
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                return attributes.getRequest();
            }
        } catch (Exception e) {
            // Se não conseguir obter o request, retorna null
        }

        return null;
    }
}
