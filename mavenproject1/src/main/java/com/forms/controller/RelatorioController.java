package com.forms.controller;

import com.forms.models.Perfil;
import com.forms.models.Usuario;
import com.forms.service.LogAuditoriaService;
import com.forms.service.RelatorioService;
import com.forms.service.annotation.Auditavel;
import com.forms.service.dto.RelatorioFormulario;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para Relatórios
 *
 * Implementa controle de acesso hierárquico para relatórios:
 *
 * RF16: Relatórios consolidados por curso, disciplina, turma e professor
 * RF17: Visualização de scores calculados
 * RF19: Respeito ao anonimato
 *
 * HIERARQUIA DE ACESSO:
 *
 * 1. ADMINISTRADOR/COORDENADOR:
 *    - Acesso TOTAL a todos os relatórios
 *    - Pode filtrar por curso, disciplina, turma, professor
 *    - Visualiza dados agregados + identificação (se não for anônimo)
 *
 * 2. PROFESSOR:
 *    - Acesso LIMITADO às turmas onde leciona
 *    - Visualiza apenas dados agregados (RF19 - sem identificação individual)
 *    - Scores e percentuais disponíveis (RF17)
 *
 * 3. ALUNO:
 *    - NÃO tem acesso a relatórios consolidados
 *    - Pode ver apenas suas próprias respostas (via AlunoController)
 *    - Apenas se formulário NÃO for anônimo
 *
 * @author gabriela
 */
@RestController
@RequestMapping("/api/relatorios")
@PreAuthorize("isAuthenticated()")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    @Autowired
    private LogAuditoriaService logService;

    /**
     * RF16, RF17: Relatório geral de um formulário
     *
     * Acesso: Apenas COORDENADOR ou ADMINISTRADOR
     */
    @GetMapping("/formulario/{formularioId}/geral")
    @PreAuthorize("hasAnyRole('COORDENADOR', 'ADMINISTRADOR')")
    @Auditavel(acao = "ACESSAR_RELATORIO_GERAL", tipo = "RELATORIO")
    public ResponseEntity<RelatorioFormulario> relatorioGeral(
            @PathVariable Integer formularioId,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        RelatorioFormulario relatorio = relatorioService.gerarRelatorioGeral(formularioId);

        logService.registrarAcessoRelatorio(
                usuario,
                formularioId,
                "GERAL",
                request
        );

        return ResponseEntity.ok(relatorio);
    }

    /**
     * RF16, RF17: Relatório por turma específica
     *
     * Acesso:
     * - COORDENADOR/ADMINISTRADOR: Todas as turmas
     * - PROFESSOR: Apenas turmas onde leciona
     */
    @GetMapping("/formulario/{formularioId}/turma/{turmaId}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADOR', 'ADMINISTRADOR')")
    @Auditavel(acao = "ACESSAR_RELATORIO_TURMA", tipo = "RELATORIO")
    public ResponseEntity<RelatorioFormulario> relatorioPorTurma(
            @PathVariable Integer formularioId,
            @PathVariable Integer turmaId,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        // Se for professor, verifica se leciona nesta turma
        if (usuario.getPerfil().getNome().equals("PROFESSOR")) {
            boolean lecionaNaTurma = usuario.getTurmasComoProfessor().stream()
                    .anyMatch(t -> t.getId().equals(turmaId));

            if (!lecionaNaTurma) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        RelatorioFormulario relatorio = relatorioService.gerarRelatorioPorTurma(formularioId, turmaId);

        logService.registrarAcessoRelatorio(
                usuario,
                formularioId,
                "TURMA",
                request
        );

        return ResponseEntity.ok(relatorio);
    }

    /**
     * RF16: Relatório consolidado por professor
     *
     * Lista relatórios de todas as turmas onde o professor leciona
     *
     * Acesso:
     * - COORDENADOR/ADMINISTRADOR: Pode ver de qualquer professor
     * - PROFESSOR: Apenas suas próprias turmas
     */
    @GetMapping("/formulario/{formularioId}/professor/{professorId}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADOR', 'ADMINISTRADOR')")
    @Auditavel(acao = "ACESSAR_RELATORIO_PROFESSOR", tipo = "RELATORIO")
    public ResponseEntity<List<RelatorioFormulario>> relatorioPorProfessor(
            @PathVariable Integer formularioId,
            @PathVariable Integer professorId,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        // Se for professor, só pode ver seus próprios relatórios
        if (usuario.getPerfil().getNome().equals("PROFESSOR")) {
            if (!usuario.getId().equals(professorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        List<RelatorioFormulario> relatorios = relatorioService.gerarRelatorioPorProfessor(
                professorId,
                formularioId
        );

        logService.registrarAcessoRelatorio(
                usuario,
                formularioId,
                "PROFESSOR",
                request
        );

        return ResponseEntity.ok(relatorios);
    }

    /**
     * RF16: Estatísticas básicas de um formulário
     *
     * Retorna apenas:
     * - Total de submissões
     * - Total de questões
     * - Se é anônimo
     *
     * Acesso: PROFESSOR, COORDENADOR, ADMINISTRADOR
     */
    @GetMapping("/formulario/{formularioId}/estatisticas")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADOR', 'ADMINISTRADOR')")
    public ResponseEntity<Map<String, Object>> estatisticasBasicas(
            @PathVariable Integer formularioId,
            Authentication authentication) {

        Map<String, Object> estatisticas = relatorioService.gerarEstatisticasBasicas(formularioId);

        return ResponseEntity.ok(estatisticas);
    }

    /**
     * Dashboard do coordenador - Resumo de todos os processos ativos
     *
     * Acesso: Apenas COORDENADOR ou ADMINISTRADOR
     */
    @GetMapping("/dashboard/coordenador")
    @PreAuthorize("hasAnyRole('COORDENADOR', 'ADMINISTRADOR')")
    public ResponseEntity<Map<String, Object>> dashboardCoordenador(Authentication authentication) {
        Usuario coordenador = (Usuario) authentication.getPrincipal();

        // Aqui você pode adicionar lógica para buscar:
        // - Processos ativos
        // - Formulários com mais respostas
        // - Estatísticas gerais

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("mensagem", "Dashboard do coordenador - A implementar");

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Dashboard do professor - Resumo das turmas e formulários
     *
     * Acesso: PROFESSOR, COORDENADOR, ADMINISTRADOR
     */
    @GetMapping("/dashboard/professor")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADOR', 'ADMINISTRADOR')")
    public ResponseEntity<Map<String, Object>> dashboardProfessor(Authentication authentication) {
        Usuario professor = (Usuario) authentication.getPrincipal();

        // Aqui você pode adicionar lógica para buscar:
        // - Formulários do professor
        // - Taxa de resposta por turma
        // - Scores médios

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("mensagem", "Dashboard do professor - A implementar");

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Comparar scores entre turmas
     *
     * Útil para coordenadores compararem performance entre turmas
     *
     * Acesso: Apenas COORDENADOR ou ADMINISTRADOR
     */
    @GetMapping("/formulario/{formularioId}/comparacao-turmas")
    @PreAuthorize("hasAnyRole('COORDENADOR', 'ADMINISTRADOR')")
    @Auditavel(acao = "COMPARAR_TURMAS", tipo = "RELATORIO")
    public ResponseEntity<Map<String, Object>> compararTurmas(
            @PathVariable Integer formularioId,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario coordenador = (Usuario) authentication.getPrincipal();

        // Gera relatório geral
        RelatorioFormulario relatorioGeral = relatorioService.gerarRelatorioGeral(formularioId);

        // Aqui você pode adicionar lógica para:
        // - Buscar relatórios de cada turma
        // - Comparar scores médios
        // - Gerar gráficos comparativos

        Map<String, Object> comparacao = new HashMap<>();
        comparacao.put("relatorioGeral", relatorioGeral);
        comparacao.put("mensagem", "Comparação entre turmas - A implementar detalhes");

        logService.registrarAcessoRelatorio(
                coordenador,
                formularioId,
                "COMPARACAO_TURMAS",
                request
        );

        return ResponseEntity.ok(comparacao);
    }

    /**
     * Exportar relatório (futura funcionalidade para PDF/Excel)
     *
     * Acesso: PROFESSOR, COORDENADOR, ADMINISTRADOR
     */
    @GetMapping("/formulario/{formularioId}/exportar")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'COORDENADOR', 'ADMINISTRADOR')")
    @Auditavel(acao = "EXPORTAR_RELATORIO", tipo = "RELATORIO")
    public ResponseEntity<Map<String, String>> exportarRelatorio(
            @PathVariable Integer formularioId,
            @RequestParam(defaultValue = "PDF") String formato,
            Authentication authentication,
            HttpServletRequest request) {

        Usuario usuario = (Usuario) authentication.getPrincipal();

        logService.registrarSucesso(
                usuario,
                "EXPORTAR_RELATORIO",
                "Relatório exportado (Formulário ID: " + formularioId + ") - Formato: " + formato,
                "RELATORIO",
                request
        );

        Map<String, String> response = new HashMap<>();
        response.put("mensagem", "Exportação para " + formato + " - Funcionalidade a implementar");
        response.put("formato", formato);

        return ResponseEntity.ok(response);
    }

    /**
     * Verificar se usuário tem acesso ao relatório
     */
    @GetMapping("/formulario/{formularioId}/verificar-acesso")
    public ResponseEntity<Map<String, Object>> verificarAcesso(
            @PathVariable Integer formularioId,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();
        Perfil perfil = usuario.getPerfil();

        Map<String, Object> response = new HashMap<>();
        response.put("formularioId", formularioId);
        response.put("perfilUsuario", perfil.getNome());

        boolean temAcesso = false;
        String nivelAcesso = "NENHUM";

        if (perfil.getNome().equals("ADMINISTRADOR") || perfil.getNome().equals("COORDENADOR")) {
            temAcesso = true;
            nivelAcesso = "TOTAL";
        } else if (perfil.getNome().equals("PROFESSOR")) {
            // Verifica se o professor tem turmas vinculadas a este formulário
            // (implementar lógica específica)
            temAcesso = true;
            nivelAcesso = "LIMITADO";
        }

        response.put("temAcesso", temAcesso);
        response.put("nivelAcesso", nivelAcesso);

        return ResponseEntity.ok(response);
    }
}
