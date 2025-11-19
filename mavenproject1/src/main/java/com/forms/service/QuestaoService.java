package com.forms.service;

import com.forms.models.Formulario;
import com.forms.models.Questao;
import com.forms.models.TipoQuestao;
import com.forms.repository.FormularioRepository;
import com.forms.repository.QuestaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service para gerenciar Questões
 *
 * Implementa:
 * - RF09: Criação de questões (abertas, múltipla escolha, caixas de seleção, likert)
 * - RF10: Marcar questões como obrigatórias
 * - Reordenação de questões
 *
 * @author gabriela
 */
@Service
public class QuestaoService {

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private FormularioRepository formularioRepository;

    /**
     * RF09: Criar nova questão
     */
    @Transactional
    public Questao criar(Integer formularioId, String texto, TipoQuestao tipo, Boolean obrigatoria) {
        Formulario formulario = formularioRepository.findById(formularioId)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        // Determina a ordem da nova questão (última posição)
        List<Questao> questoesExistentes = questaoRepository.findByFormularioIdOrderByOrdemAsc(formularioId);
        Integer novaOrdem = questoesExistentes.size() + 1;

        Questao questao = new Questao();
        questao.setFormulario(formulario);
        questao.setTexto(texto);
        questao.setTipo(tipo);
        questao.setObrigatoria(obrigatoria != null ? obrigatoria : false);
        questao.setOrdem(novaOrdem);

        return questaoRepository.save(questao);
    }

    /**
     * RF09: Atualizar questão
     */
    @Transactional
    public Questao atualizar(Integer questaoId, String texto, Boolean obrigatoria) {
        Questao questao = questaoRepository.findById(questaoId)
                .orElseThrow(() -> new IllegalArgumentException("Questão não encontrada"));

        if (texto != null && !texto.trim().isEmpty()) {
            questao.setTexto(texto);
        }

        if (obrigatoria != null) {
            questao.setObrigatoria(obrigatoria);
        }

        return questaoRepository.save(questao);
    }

    /**
     * RF09: Deletar questão
     */
    @Transactional
    public void deletar(Integer questaoId) {
        Questao questao = questaoRepository.findById(questaoId)
                .orElseThrow(() -> new IllegalArgumentException("Questão não encontrada"));

        Integer formularioId = questao.getFormulario().getId();
        Integer ordemDeletada = questao.getOrdem();

        // Deleta a questão
        questaoRepository.delete(questao);

        // Reordena as questões seguintes
        List<Questao> questoesPosteriores = questaoRepository.findByFormularioIdOrderByOrdemAsc(formularioId);
        for (Questao q : questoesPosteriores) {
            if (q.getOrdem() > ordemDeletada) {
                q.setOrdem(q.getOrdem() - 1);
                questaoRepository.save(q);
            }
        }
    }

    /**
     * RF09: Reordenar questões
     *
     * @param formularioId ID do formulário
     * @param novaOrdem Map<QuestaoId, NovaOrdem>
     */
    @Transactional
    public List<Questao> reordenar(Integer formularioId, Map<Integer, Integer> novaOrdem) {
        for (Map.Entry<Integer, Integer> entry : novaOrdem.entrySet()) {
            Integer questaoId = entry.getKey();
            Integer ordem = entry.getValue();

            Questao questao = questaoRepository.findById(questaoId)
                    .orElseThrow(() -> new IllegalArgumentException("Questão não encontrada: " + questaoId));

            questao.setOrdem(ordem);
            questaoRepository.save(questao);
        }

        return questaoRepository.findByFormularioIdOrderByOrdemAsc(formularioId);
    }

    /**
     * Listar questões de um formulário
     */
    public List<Questao> listarPorFormulario(Integer formularioId) {
        return questaoRepository.findByFormularioIdOrderByOrdemAsc(formularioId);
    }

    /**
     * RF10: Listar questões obrigatórias de um formulário
     */
    public List<Questao> listarObrigatorias(Integer formularioId) {
        return questaoRepository.findQuestoesObrigatoriasPorFormulario(formularioId);
    }

    /**
     * Buscar questão por ID
     */
    public Questao buscarPorId(Integer questaoId) {
        return questaoRepository.findById(questaoId)
                .orElseThrow(() -> new IllegalArgumentException("Questão não encontrada"));
    }

    /**
     * Contar questões de um formulário
     */
    public Long contarPorFormulario(Integer formularioId) {
        return questaoRepository.countByFormularioId(formularioId);
    }

    /**
     * RF10: Marcar questão como obrigatória
     */
    @Transactional
    public Questao marcarComoObrigatoria(Integer questaoId, Boolean obrigatoria) {
        Questao questao = questaoRepository.findById(questaoId)
                .orElseThrow(() -> new IllegalArgumentException("Questão não encontrada"));

        questao.setObrigatoria(obrigatoria);
        return questaoRepository.save(questao);
    }
}
