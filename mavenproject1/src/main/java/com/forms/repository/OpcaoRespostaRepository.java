package com.forms.repository;

import com.forms.models.Questao;
import com.forms.models.OpcaoResposta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OpcaoRespostaRepository extends JpaRepository<OpcaoResposta, Integer> {
    List<OpcaoResposta> findByQuestao(Questao questao);

    Optional<OpcaoResposta> findByQuestaoAndIsCorreta(Questao questao, boolean valor);
}