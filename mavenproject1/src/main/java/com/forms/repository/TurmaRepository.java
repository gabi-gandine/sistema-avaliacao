package com.forms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.forms.models.Turma;
import com.forms.models.Usuario;

import java.util.List;

public interface TurmaRepository extends JpaRepository<Turma, Integer> {
    
    // Método para RF05
    List<Turma> findByProfessor(Usuario professor);

    // Método para RF06
    List<Turma> findByAluno(Usuario aluno);
}