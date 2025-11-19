package com.forms.repository;

import com.forms.models.Alternativa;
import com.forms.models.Questao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository para Alternativa
 * Fornece queries para buscar alternativas e calcular scores
 */
public interface AlternativaRepository extends JpaRepository<Alternativa, Integer> {

    /**
     * Busca todas as alternativas de uma questão específica, ordenadas
     */
    List<Alternativa> findByQuestaoOrderByOrdemAsc(Questao questao);

    /**
     * Busca alternativas de uma questão por ID
     */
    @Query("SELECT a FROM Alternativa a WHERE a.questao.id = :questaoId ORDER BY a.ordem ASC")
    List<Alternativa> findByQuestaoId(@Param("questaoId") Integer questaoId);

    /**
     * Busca alternativas corretas de uma questão (para questões tipo quiz/prova)
     */
    @Query("SELECT a FROM Alternativa a WHERE a.questao.id = :questaoId AND a.isCorreta = true")
    List<Alternativa> findAlternativasCorretasPorQuestao(@Param("questaoId") Integer questaoId);

    /**
     * RF17: Busca alternativas que possuem peso definido (para cálculo de scores)
     */
    @Query("SELECT a FROM Alternativa a WHERE a.questao.id = :questaoId AND a.peso IS NOT NULL")
    List<Alternativa> findAlternativasComPeso(@Param("questaoId") Integer questaoId);

    /**
     * Conta quantas alternativas uma questão possui
     */
    @Query("SELECT COUNT(a) FROM Alternativa a WHERE a.questao.id = :questaoId")
    Long countByQuestaoId(@Param("questaoId") Integer questaoId);

    /**
     * Busca a próxima ordem disponível para uma nova alternativa em uma questão
     */
    @Query("SELECT COALESCE(MAX(a.ordem), 0) + 1 FROM Alternativa a WHERE a.questao.id = :questaoId")
    Integer findProximaOrdem(@Param("questaoId") Integer questaoId);

    /**
     * Verifica se uma questão tem pelo menos uma alternativa correta
     */
    @Query("SELECT COUNT(a) > 0 FROM Alternativa a WHERE a.questao.id = :questaoId AND a.isCorreta = true")
    boolean questaoTemAlternativaCorreta(@Param("questaoId") Integer questaoId);
}
