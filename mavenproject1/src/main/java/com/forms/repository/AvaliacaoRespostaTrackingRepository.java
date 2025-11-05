package com.forms.repository;

import com.forms.models.AvaliacaoRespostaTracking;
import com.forms.models.Avaliacao;
import com.forms.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository para tracking de respostas (RF03)
 */
public interface AvaliacaoRespostaTrackingRepository extends JpaRepository<AvaliacaoRespostaTracking, Integer> {

    /**
     * Verifica se um usuário já respondeu uma avaliação
     */
    boolean existsByAvaliacaoAndUsuario(Avaliacao avaliacao, Usuario usuario);

    /**
     * Busca o tracking de resposta de um usuário em uma avaliação
     */
    Optional<AvaliacaoRespostaTracking> findByAvaliacaoAndUsuario(Avaliacao avaliacao, Usuario usuario);

    /**
     * Lista todos que responderam uma avaliação
     */
    List<AvaliacaoRespostaTracking> findByAvaliacao(Avaliacao avaliacao);

    /**
     * Lista todas as avaliações respondidas por um usuário
     */
    List<AvaliacaoRespostaTracking> findByUsuario(Usuario usuario);

    /**
     * Conta quantas pessoas responderam uma avaliação
     */
    @Query("SELECT COUNT(art) FROM AvaliacaoRespostaTracking art WHERE art.avaliacao.id = :avaliacaoId AND art.completa = true")
    Long countRespostasCompletasByAvaliacaoId(@Param("avaliacaoId") Integer avaliacaoId);

    /**
     * Lista usuários que completaram uma avaliação
     */
    @Query("SELECT art FROM AvaliacaoRespostaTracking art WHERE art.avaliacao.id = :avaliacaoId AND art.completa = true")
    List<AvaliacaoRespostaTracking> findRespostasCompletasByAvaliacaoId(@Param("avaliacaoId") Integer avaliacaoId);
}
