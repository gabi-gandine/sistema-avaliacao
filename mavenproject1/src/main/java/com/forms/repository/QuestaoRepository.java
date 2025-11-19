package com.forms.repository;

import com.forms.models.Questao;
import com.forms.models.Formulario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository para Questao
 * Suporta tanto modelo legado (Avaliacao) quanto novo (Formulario)
 */
public interface QuestaoRepository extends JpaRepository<Questao, Integer> {

    /**
     * NOVO: Busca questões de um formulário ordenadas
     */
    List<Questao> findByFormularioOrderByOrdemAsc(Formulario formulario);

    /**
     * NOVO: Busca questões de um formulário por ID ordenadas
     */
    @Query("SELECT q FROM Questao q WHERE q.formulario.id = :formularioId ORDER BY q.ordem ASC")
    List<Questao> findByFormularioIdOrderByOrdemAsc(@Param("formularioId") Integer formularioId);

    /**
     * RF10: Busca apenas questões obrigatórias de um formulário
     */
    @Query("SELECT q FROM Questao q WHERE q.formulario.id = :formularioId AND q.obrigatoria = true ORDER BY q.ordem ASC")
    List<Questao> findQuestoesObrigatoriasPorFormulario(@Param("formularioId") Integer formularioId);

    /**
     * Busca apenas questões opcionais de um formulário
     */
    @Query("SELECT q FROM Questao q WHERE q.formulario.id = :formularioId AND q.obrigatoria = false ORDER BY q.ordem ASC")
    List<Questao> findQuestoesOpcionaisPorFormulario(@Param("formularioId") Integer formularioId);

    /**
     * Conta quantas questões um formulário tem
     */
    @Query("SELECT COUNT(q) FROM Questao q WHERE q.formulario.id = :formularioId")
    Long countByFormularioId(@Param("formularioId") Integer formularioId);

    /**
     * Busca a próxima ordem disponível para uma nova questão em um formulário
     */
    @Query("SELECT COALESCE(MAX(q.ordem), 0) + 1 FROM Questao q WHERE q.formulario.id = :formularioId")
    Integer findProximaOrdem(@Param("formularioId") Integer formularioId);
}
