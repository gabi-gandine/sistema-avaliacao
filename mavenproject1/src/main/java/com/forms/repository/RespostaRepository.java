package com.forms.repository;

import com.forms.models.Resposta;
import com.forms.models.Questao;
import com.forms.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RespostaRepository extends JpaRepository<Resposta, Integer> {

    List<Resposta> findByUsuario(Usuario usuario);

    List<Resposta> findByQuestao(Questao questao);

    Optional<Resposta> findByQuestaoAndUsuario(Questao questao, Usuario usuario);

    @Query("SELECT r FROM Resposta r WHERE r.questao.avaliacao.id = :avaliacaoId AND r.usuario.id = :usuarioId")
    List<Resposta> findByAvaliacaoIdAndUsuarioId(@Param("avaliacaoId") Integer avaliacaoId, @Param("usuarioId") Integer usuarioId);

    @Query("SELECT COUNT(r) FROM Resposta r WHERE r.questao.avaliacao.id = :avaliacaoId")
    Long countRespostasByAvaliacaoId(@Param("avaliacaoId") Integer avaliacaoId);
}
