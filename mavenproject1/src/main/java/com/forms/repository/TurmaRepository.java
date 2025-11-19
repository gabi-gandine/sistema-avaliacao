package com.forms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.forms.models.Turma;
import com.forms.models.Usuario;

import java.util.List;

public interface TurmaRepository extends JpaRepository<Turma, Integer> {

    /**
     * RF05: Busca turmas onde o professor está na coleção de professores
     * Usa MEMBER OF para buscar em relacionamento many-to-many
     */
    @Query("SELECT t FROM Turma t WHERE :professor MEMBER OF t.professores")
    List<Turma> findByProfessor(@Param("professor") Usuario professor);

    /**
     * RF06: Busca turmas onde o aluno está na coleção de alunos
     * Usa MEMBER OF para buscar em relacionamento many-to-many
     */
    @Query("SELECT t FROM Turma t WHERE :aluno MEMBER OF t.alunos")
    List<Turma> findByAlunos(@Param("aluno") Usuario aluno);
}