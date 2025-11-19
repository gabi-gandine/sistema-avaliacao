package com.forms.repository;

import com.forms.models.Formulario;
import com.forms.models.SubmissaoControle;
import com.forms.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository para SubmissaoControle
 * Queries críticas para controle de submissões e resposta única (RF13)
 */
public interface SubmissaoControleRepository extends JpaRepository<SubmissaoControle, Integer> {

    /**
     * RF13: CRÍTICO - Verifica se um usuário já respondeu um formulário específico
     * Usado para garantir resposta única
     */
    @Query("SELECT COUNT(s) > 0 FROM SubmissaoControle s " +
           "WHERE s.formulario.id = :formularioId AND s.usuario.id = :usuarioId")
    boolean jaRespondeu(@Param("formularioId") Integer formularioId, @Param("usuarioId") Integer usuarioId);

    /**
     * Busca a submissão de um usuário para um formulário específico
     * Retorna Optional vazio se não existir
     */
    @Query("SELECT s FROM SubmissaoControle s " +
           "WHERE s.formulario.id = :formularioId AND s.usuario.id = :usuarioId")
    Optional<SubmissaoControle> findByFormularioIdAndUsuarioId(
            @Param("formularioId") Integer formularioId,
            @Param("usuarioId") Integer usuarioId
    );

    /**
     * Busca todas as submissões de um usuário
     */
    List<SubmissaoControle> findByUsuario(Usuario usuario);

    /**
     * Busca todas as submissões de um formulário
     */
    List<SubmissaoControle> findByFormulario(Formulario formulario);

    /**
     * Busca submissões completas (finalizadas) de um formulário
     */
    @Query("SELECT s FROM SubmissaoControle s " +
           "WHERE s.formulario.id = :formularioId AND s.completa = true")
    List<SubmissaoControle> findSubmissoesCompletasPorFormulario(@Param("formularioId") Integer formularioId);

    /**
     * Busca submissões incompletas (rascunhos) de um formulário
     */
    @Query("SELECT s FROM SubmissaoControle s " +
           "WHERE s.formulario.id = :formularioId AND s.completa = false")
    List<SubmissaoControle> findSubmissoesIncompletasPorFormulario(@Param("formularioId") Integer formularioId);

    /**
     * Conta quantas pessoas responderam um formulário
     */
    @Query("SELECT COUNT(s) FROM SubmissaoControle s " +
           "WHERE s.formulario.id = :formularioId AND s.completa = true")
    Long countSubmissoesCompletas(@Param("formularioId") Integer formularioId);

    /**
     * Taxa de resposta: quantos responderam vs quantos poderiam responder
     * Usado para estatísticas do formulário
     */
    @Query("SELECT COUNT(s) FROM SubmissaoControle s " +
           "WHERE s.formulario.id = :formularioId")
    Long countTotalSubmissoes(@Param("formularioId") Integer formularioId);

    /**
     * Busca submissões por IP (para detecção de fraudes)
     */
    @Query("SELECT s FROM SubmissaoControle s WHERE s.ipAddress = :ip")
    List<SubmissaoControle> findByIpAddress(@Param("ip") String ip);

    /**
     * Busca submissões de um formulário que ainda podem ser editadas
     * RF13: Apenas formulários não-anônimos e que permitem edição
     */
    @Query("SELECT s FROM SubmissaoControle s " +
           "WHERE s.formulario.id = :formularioId " +
           "AND s.usuario.id = :usuarioId " +
           "AND s.formulario.isAnonimo = false " +
           "AND s.formulario.permiteEdicao = true")
    Optional<SubmissaoControle> findSubmissaoEditavel(
            @Param("formularioId") Integer formularioId,
            @Param("usuarioId") Integer usuarioId
    );

    /**
     * Busca todas as submissões de um usuário que estão incompletas
     * Útil para mostrar "formulários pendentes"
     */
    @Query("SELECT s FROM SubmissaoControle s " +
           "WHERE s.usuario.id = :usuarioId AND s.completa = false")
    List<SubmissaoControle> findSubmissoesPendentesPorUsuario(@Param("usuarioId") Integer usuarioId);

    /**
     * RNF04: Busca submissões para auditoria
     * Ordenadas por data de criação
     */
    List<SubmissaoControle> findAllByOrderByCreatedAtDesc();

    /**
     * Busca submissões por turma (para relatórios por turma)
     */
    @Query("SELECT s FROM SubmissaoControle s " +
           "WHERE s.turma.id = :turmaId AND s.completa = true")
    List<SubmissaoControle> findSubmissoesCompletasPorTurma(@Param("turmaId") Integer turmaId);

    /**
     * Busca submissões de um formulário em uma turma específica
     */
    @Query("SELECT s FROM SubmissaoControle s " +
           "WHERE s.formulario.id = :formularioId " +
           "AND s.turma.id = :turmaId " +
           "AND s.completa = true")
    List<SubmissaoControle> findSubmissoesPorFormularioETurma(
            @Param("formularioId") Integer formularioId,
            @Param("turmaId") Integer turmaId
    );
}
