package com.forms.service;

import com.forms.models.Curso;
import com.forms.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CursoService {

    @Autowired
    private CursoRepository cursoRepository;

    /**
     * Salva ou atualiza um curso (RF04).
     */
    @Transactional
    public Curso salvar(Curso curso) {
        if (curso.getId() == null && cursoRepository.findByNome(curso.getNome()).isPresent()) {
             throw new IllegalArgumentException("Já existe um curso com este nome.");
        }
        return cursoRepository.save(curso);
    }

    /**
     * Busca um curso por ID.
     */
    public Optional<Curso> buscarPorId(Integer id) {
        return cursoRepository.findById(id);
    }

    /**
     * Lista todos os cursos (RF04).
     */
    public List<Curso> listarTodos() {
        return cursoRepository.findAll();
    }

    /**
     * Deleta um curso por ID (RF04).
     */
    @Transactional
    public void deletar(Integer id) {
        if (!cursoRepository.existsById(id)) {
            throw new IllegalArgumentException("Curso não encontrado.");
        }
        cursoRepository.deleteById(id);
    }

    /**
     * Cria um novo curso (alias para salvar - compatibilidade com controller).
     */
    @Transactional
    public Curso criar(Curso curso) {
        return salvar(curso);
    }

    /**
     * Atualiza um curso existente (compatibilidade com controller).
     */
    @Transactional
    public Curso atualizar(Integer id, Curso cursoAtualizado) {
        Curso cursoExistente = buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso não encontrado com ID: " + id));

        // Atualiza os campos
        cursoExistente.setNome(cursoAtualizado.getNome());

        return cursoRepository.save(cursoExistente);
    }
}
