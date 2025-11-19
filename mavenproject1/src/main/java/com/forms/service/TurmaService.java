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

    @Autowired
    private UsuarioService usuarioService;

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

    /**
     * Cria uma nova turma (alias para salvar - compatibilidade com controller).
     */
    @Transactional
    public Turma criar(Turma turma) {
        return salvar(turma);
    }

    /**
     * Atualiza uma turma existente (compatibilidade com controller).
     */
    @Transactional
    public Turma atualizar(Integer id, Turma turmaAtualizada) {
        Turma turmaExistente = buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada com ID: " + id));

        // Atualiza os campos
        turmaExistente.setAno(turmaAtualizada.getAno());
        turmaExistente.setSemestre(turmaAtualizada.getSemestre());
        turmaExistente.setUc(turmaAtualizada.getUc());

        return turmaRepository.save(turmaExistente);
    }

    /**
     * Vincula um professor a uma turma (RF05).
     */
    @Transactional
    public Turma vincularProfessor(Integer turmaId, Usuario professor) {
        Turma turma = buscarPorId(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada com ID: " + turmaId));

        turma.adicionarProfessor(professor);
        return turmaRepository.save(turma);
    }

    /**
     * Desvincula o professor de uma turma (RF05).
     */
    @Transactional
    public Turma desvincularProfessor(Integer turmaId, Integer professorId) {
        Turma turma = buscarPorId(turmaId)
                .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada com ID: " + turmaId));

        Usuario professor = usuarioService.buscarPorId(professorId)
                .orElseThrow(() -> new IllegalArgumentException("Professor não encontrado com ID: " + professorId));

        turma.removerProfessor(professor);
        return turmaRepository.save(turma);
    }
}
