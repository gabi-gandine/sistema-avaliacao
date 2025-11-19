package com.forms.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation para marcar métodos que devem ser auditados automaticamente
 *
 * Uso:
 * @Auditavel(acao = "CRIAR_FORMULARIO", tipo = "FORMULARIO")
 * public Formulario criar(...) { ... }
 *
 * O AOP Aspect interceptará o método e registrará no log de auditoria
 *
 * @author gabriela
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditavel {

    /**
     * Nome da ação sendo auditada
     * Ex: "LOGIN", "CRIAR_FORMULARIO", "SUBMETER_RESPOSTA"
     */
    String acao();

    /**
     * Tipo/categoria da ação
     * Ex: "AUTENTICACAO", "FORMULARIO", "RESPOSTA", "RELATORIO"
     */
    String tipo() default "GERAL";

    /**
     * Descrição detalhada (opcional)
     * Se não fornecida, será gerada automaticamente
     */
    String descricao() default "";
}
