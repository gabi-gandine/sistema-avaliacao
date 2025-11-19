package com.forms.repository;

import com.forms.models.RespostaAgrupador;
import com.forms.models.SubmissaoControle;
import com.forms.models.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository para RespostaAgrupador
 * Queries para relatórios que respeitam anonimato
 */
public interface RespostaAgrupadorRepository extends JpaRepository<RespostaAgrupador, Integer> {

    /**
     * Busca o RespostaAgrupador de uma submissão específica
     */
    Optional<RespostaAgrupador> findBySubmissaoControle(SubmissaoControle submissaoControle);

    /**
     * Busca todos os RespostaAgrupador de uma turma
     * Usado para relatórios agregados por turma
     */
    List<RespostaAgrupador> findByTurma(Turma turma);

    /**
     * RF16: Busca respostas agrupadas por turma E formulário
     * IMPORTANTE: Esta query NÃO acessa o usuário, respeitando anonimato!
     */
    @Query("SELECT ra FROM RespostaAgrupador ra " +
           "WHERE ra.turma.id = :turmaId " +
           "AND ra.submissaoControle.formulario.id = :formularioId " +
           "AND ra.finalizadoAt IS NOT NULL")
    List<RespostaAgrupador> findRespostasFinalizadasPorTurmaEFormulario(
            @Param("turmaId") Integer turmaId,
            @Param("formularioId") Integer formularioId
    );

    /**
     * Busca apenas respostas finalizadas
     */
    @Query("SELECT ra FROM RespostaAgrupador ra WHERE ra.finalizadoAt IS NOT NULL")
    List<RespostaAgrupador> findAllFinalizados();

    /**
     * Busca respostas finalizadas de um formulário específico
     * Usado para gerar relatórios gerais do formulário
     */
    @Query("SELECT ra FROM RespostaAgrupador ra " +
           "WHERE ra.submissaoControle.formulario.id = :formularioId " +
           "AND ra.finalizadoAt IS NOT NULL")
    List<RespostaAgrupador> findRespostasFinalizadasPorFormulario(@Param("formularioId") Integer formularioId);

    /**
     * Conta quantas respostas finalizadas um formulário tem
     */
    @Query("SELECT COUNT(ra) FROM RespostaAgrupador ra " +
           "WHERE ra.submissaoControle.formulario.id = :formularioId " +
           "AND ra.finalizadoAt IS NOT NULL")
    Long countRespostasFinalizadasPorFormulario(@Param("formularioId") Integer formularioId);

    /**
     * Conta quantas respostas finalizadas uma turma tem para um formulário
     */
    @Query("SELECT COUNT(ra) FROM RespostaAgrupador ra " +
           "WHERE ra.turma.id = :turmaId " +
           "AND ra.submissaoControle.formulario.id = :formularioId " +
           "AND ra.finalizadoAt IS NOT NULL")
    Long countRespostasPorTurmaEFormulario(
            @Param("turmaId") Integer turmaId,
            @Param("formularioId") Integer formularioId
    );

    /**
     * Busca respostas não finalizadas (rascunhos)
     */
    @Query("SELECT ra FROM RespostaAgrupador ra WHERE ra.finalizadoAt IS NULL")
    List<RespostaAgrupador> findRascunhos();

    /**
     * APENAS PARA AUDITORIA: Busca RespostaAgrupador por usuário
     * NÃO usar em relatórios de formulários anônimos!
     */
    @Query("SELECT ra FROM RespostaAgrupador ra " +
           "WHERE ra.submissaoControle.usuario.id = :usuarioId")
    List<RespostaAgrupador> findByUsuarioIdParaAuditoria(@Param("usuarioId") Integer usuarioId);
}
