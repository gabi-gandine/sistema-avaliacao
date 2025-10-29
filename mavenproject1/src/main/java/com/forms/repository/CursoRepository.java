package com.forms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forms.models.Curso;

public interface CursoRepository extends JpaRepository<Curso, Integer> {
    
}
