package com.forms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.forms.models.Curso;
import java.util.Optional;

public interface CursoRepository extends JpaRepository<Curso, Integer> {
    
    // Adiciona busca por nome para validação (RF04)
    Optional<Curso> findByNome(String nome);
}