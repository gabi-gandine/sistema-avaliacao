package com.forms.service;

import com.forms.models.UnidadeCurricular;
import com.forms.repository.UnidadeCurricularRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UnidadeCurricularService {

    @Autowired
    private UnidadeCurricularRepository ucRepository;

    /**
     * Salva ou atualiza uma Unidade Curricular (RF04).
     */
    @Transactional
    public UnidadeCurricular salvar(UnidadeCurricular uc) {
        return ucRepository.save(uc);
    }

    /**
     * Busca uma UC por ID.
     */
    public Optional<UnidadeCurricular> buscarPorId(Integer id) {
        return ucRepository.findById(id);
    }

    /**
     * Lista todas as Unidades Curriculares (RF04).
     */
    public List<UnidadeCurricular> listarTodos() {
        return ucRepository.findAll();
    }

    /**
     * Deleta uma UC por ID (RF04).
     */
    @Transactional
    public void deletar(Integer id) {
        if (!ucRepository.existsById(id)) {
            throw new IllegalArgumentException("Unidade Curricular não encontrada.");
        }
        ucRepository.deleteById(id);
    }

    /**
     * Lista todas as Unidades Curriculares (alias para compatibilidade com controller).
     */
    public List<UnidadeCurricular> listarTodas() {
        return listarTodos();
    }

    /**
     * Cria uma nova Unidade Curricular (alias para salvar - compatibilidade com controller).
     */
    @Transactional
    public UnidadeCurricular criar(UnidadeCurricular uc) {
        return salvar(uc);
    }

    /**
     * Atualiza uma Unidade Curricular existente (compatibilidade com controller).
     */
    @Transactional
    public UnidadeCurricular atualizar(Integer id, UnidadeCurricular ucAtualizada) {
        UnidadeCurricular ucExistente = buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Unidade Curricular não encontrada com ID: " + id));

        // Atualiza os campos
        ucExistente.setNome(ucAtualizada.getNome());
        ucExistente.setCurso(ucAtualizada.getCurso());

        return ucRepository.save(ucExistente);
    }
}
