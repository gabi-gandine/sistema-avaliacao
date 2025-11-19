package com.forms.repository;

import com.forms.models.Resposta;
import com.forms.models.Questao;
import com.forms.models.RespostaAgrupador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para Resposta
 * Queries adaptadas para trabalhar com RespostaAgrupador (novo modelo)
 */
public interface RespostaRepository extends JpaRepository<Resposta, Integer> {

    /**
     * Busca todas as respostas de uma questão
     */
    List<Resposta> findByQuestao(Questao questao);

    /**
     * NOVO: Busca resposta de um agrupador para uma questão específica
     * Permite verificar se uma questão já foi respondida durante a submissão
     */
    Optional<Resposta> findByRespostaAgrupadorAndQuestao(RespostaAgrupador agrupador, Questao questao);

    /**
     * Busca todas as respostas de um RespostaAgrupador
     */
    List<Resposta> findByRespostaAgrupador(RespostaAgrupador agrupador);

}
