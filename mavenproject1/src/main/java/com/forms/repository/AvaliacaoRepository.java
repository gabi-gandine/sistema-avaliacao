package com.forms.repository;

import com.forms.models.Avaliacao;
import com.forms.models.Turma;
import com.forms.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Integer> {

    List<Avaliacao> findByTurma(Turma turma);

    List<Avaliacao> findByCriador(Usuario criador);

    @Query("SELECT a FROM Avaliacao a WHERE a.dataInicio <= :agora AND a.dataFim >= :agora")
    List<Avaliacao> findAvaliacoesAtivas(@Param("agora") LocalDateTime agora);

    @Query("SELECT a FROM Avaliacao a WHERE a.turma.id = :turmaId AND a.dataInicio <= :agora AND a.dataFim >= :agora")
    List<Avaliacao> findAvaliacoesAtivasPorTurma(@Param("turmaId") Integer turmaId, @Param("agora") LocalDateTime agora);
}
