package com.forms.service;

import com.forms.models.Alternativa;
import com.forms.models.Questao;
import com.forms.repository.AlternativaRepository;
import com.forms.repository.QuestaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service para gerenciar Alternativas de Questões
 *
 * Implementa:
 * - RF09: Criação de alternativas para questões de múltipla escolha
 * - RF17: Atribuição de pesos para cálculo de scores
 * - Suporte a escalas Likert-5
 *
 * @author gabriela
 */
@Service
public class AlternativaService {

    @Autowired
    private AlternativaRepository alternativaRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    /**
     * RF09: Criar nova alternativa
     */
    @Transactional
    public Alternativa criar(Integer questaoId, String textoAlternativa, BigDecimal peso, Boolean isCorreta) {
        Questao questao = questaoRepository.findById(questaoId)
                .orElseThrow(() -> new IllegalArgumentException("Questão não encontrada"));

        // Determina a ordem da nova alternativa (última posição)
        List<Alternativa> alternativasExistentes = alternativaRepository.findByQuestaoId(questaoId);
        Integer novaOrdem = alternativasExistentes.size() + 1;

        Alternativa alternativa = new Alternativa();
        alternativa.setQuestao(questao);
        alternativa.setTextoAlternativa(textoAlternativa);
        alternativa.setOrdem(novaOrdem);
        alternativa.setPeso(peso);
        alternativa.setIsCorreta(isCorreta != null ? isCorreta : false);

        return alternativaRepository.save(alternativa);
    }

    /**
     * RF17: Criar alternativa com peso Likert-5
     *
     * Escalas Likert comuns:
     * 1 = Discordo Totalmente (peso 1.0)
     * 2 = Discordo (peso 2.0)
     * 3 = Neutro (peso 3.0)
     * 4 = Concordo (peso 4.0)
     * 5 = Concordo Totalmente (peso 5.0)
     *
     * @param questaoId ID da questão
     * @param textoAlternativa Texto da alternativa (ex: "Concordo Totalmente")
     * @param posicaoLikert Posição na escala (1 a 5)
     */
    @Transactional
    public Alternativa criarLikert(Integer questaoId, String textoAlternativa, Integer posicaoLikert) {
        if (posicaoLikert < 1 || posicaoLikert > 5) {
            throw new IllegalArgumentException("Posição Likert deve estar entre 1 e 5");
        }

        Questao questao = questaoRepository.findById(questaoId)
                .orElseThrow(() -> new IllegalArgumentException("Questão não encontrada"));

        List<Alternativa> alternativasExistentes = alternativaRepository.findByQuestaoId(questaoId);
        Integer novaOrdem = alternativasExistentes.size() + 1;

        Alternativa alternativa = new Alternativa();
        alternativa.setQuestao(questao);
        alternativa.setTextoAlternativa(textoAlternativa);
        alternativa.setOrdem(novaOrdem);
        alternativa.setPesoLikert5(posicaoLikert); // Helper method que converte para BigDecimal
        alternativa.setIsCorreta(false); // Likert não tem resposta "correta"

        return alternativaRepository.save(alternativa);
    }

    /**
     * RF09: Atualizar alternativa
     */
    @Transactional
    public Alternativa atualizar(Integer alternativaId, String textoAlternativa, BigDecimal peso, Boolean isCorreta) {
        Alternativa alternativa = alternativaRepository.findById(alternativaId)
                .orElseThrow(() -> new IllegalArgumentException("Alternativa não encontrada"));

        if (textoAlternativa != null && !textoAlternativa.trim().isEmpty()) {
            alternativa.setTextoAlternativa(textoAlternativa);
        }

        if (peso != null) {
            alternativa.setPeso(peso);
        }

        if (isCorreta != null) {
            alternativa.setIsCorreta(isCorreta);
        }

        return alternativaRepository.save(alternativa);
    }

    /**
     * RF09: Deletar alternativa
     */
    @Transactional
    public void deletar(Integer alternativaId) {
        Alternativa alternativa = alternativaRepository.findById(alternativaId)
                .orElseThrow(() -> new IllegalArgumentException("Alternativa não encontrada"));

        Integer questaoId = alternativa.getQuestao().getId();
        Integer ordemDeletada = alternativa.getOrdem();

        // Deleta a alternativa
        alternativaRepository.delete(alternativa);

        // Reordena as alternativas seguintes
        List<Alternativa> alternativasPosteriores = alternativaRepository.findByQuestaoId(questaoId);
        for (Alternativa a : alternativasPosteriores) {
            if (a.getOrdem() > ordemDeletada) {
                a.setOrdem(a.getOrdem() - 1);
                alternativaRepository.save(a);
            }
        }
    }

    /**
     * Listar alternativas de uma questão
     */
    public List<Alternativa> listarPorQuestao(Integer questaoId) {
        return alternativaRepository.findByQuestaoId(questaoId);
    }

    /**
     * Buscar alternativa por ID
     */
    public Alternativa buscarPorId(Integer alternativaId) {
        return alternativaRepository.findById(alternativaId)
                .orElseThrow(() -> new IllegalArgumentException("Alternativa não encontrada"));
    }

    /**
     * RF17: Definir peso para alternativa
     */
    @Transactional
    public Alternativa definirPeso(Integer alternativaId, BigDecimal peso) {
        Alternativa alternativa = alternativaRepository.findById(alternativaId)
                .orElseThrow(() -> new IllegalArgumentException("Alternativa não encontrada"));

        alternativa.setPeso(peso);
        return alternativaRepository.save(alternativa);
    }

    /**
     * Marcar alternativa como correta (para questões de prova/quiz)
     */
    @Transactional
    public Alternativa marcarComoCorreta(Integer alternativaId, Boolean isCorreta) {
        Alternativa alternativa = alternativaRepository.findById(alternativaId)
                .orElseThrow(() -> new IllegalArgumentException("Alternativa não encontrada"));

        alternativa.setIsCorreta(isCorreta);
        return alternativaRepository.save(alternativa);
    }

    /**
     * Criar escala Likert-5 completa para uma questão
     *
     * Cria automaticamente as 5 alternativas padrão:
     * 1. Discordo Totalmente (peso 1)
     * 2. Discordo (peso 2)
     * 3. Neutro (peso 3)
     * 4. Concordo (peso 4)
     * 5. Concordo Totalmente (peso 5)
     */
    @Transactional
    public List<Alternativa> criarEscalaLikert5Completa(Integer questaoId) {
        String[] textos = {
                "Discordo Totalmente",
                "Discordo",
                "Neutro",
                "Concordo",
                "Concordo Totalmente"
        };

        for (int i = 0; i < textos.length; i++) {
            criarLikert(questaoId, textos[i], i + 1);
        }

        return alternativaRepository.findByQuestaoId(questaoId);
    }
}
