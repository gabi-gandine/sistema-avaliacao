package com.forms.service;

import com.forms.models.Turma;
import com.forms.models.Usuario;
import com.forms.repository.TurmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TurmaService {

    @Autowired
    private TurmaRepository turmaRepository;

    /**
     * Salva ou atualiza uma Turma (RF05, RF06).
     */
    @Transactional
    public Turma salvar(Turma turma) {
        return turmaRepository.save(turma);
    }

    /**
     * Busca uma Turma por ID.
     */
    public Optional<Turma> buscarPorId(Integer id) {
        return turmaRepository.findById(id);
    }

    /**
     * Lista todas as Turmas (RF05).
     */
    public List<Turma> listarTodas() {
        return turmaRepository.findAll();
    }

    /**
     * Busca turmas onde o usuário é professor (RF05).
     */
    public List<Turma> buscarPorProfessor(Usuario professor) {
        return turmaRepository.findByProfessor(professor);
    }

    /**
     * Busca turmas onde o usuário é aluno (RF06).
     */
    public List<Turma> buscarPorAluno(Usuario aluno) {
        return turmaRepository.findByAlunos(aluno);
    }

    /**
     * Deleta uma Turma por ID (RF05).
     */
    @Transactional
    public void deletar(Integer id) {
        if (!turmaRepository.existsById(id)) {
            throw new IllegalArgumentException("Turma não encontrada.");
        }
        turmaRepository.deleteById(id);
    }
}
