package com.forms.repository;

import com.forms.models.Questao;
import com.forms.models.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestaoRepository extends JpaRepository<Questao, Integer> {

    List<Questao> findByAvaliacaoOrderByOrdemAsc(Avaliacao avaliacao);

    List<Questao> findByAvaliacaoOrderByOrdemDesc(Avaliacao avaliacao);

    List<Questao> findByAvaliacaoIdOrderByOrdemAsc(Integer avaliacaoId);
}
