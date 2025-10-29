package com.forms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forms.models.Turma;

public interface TurmaRepository extends JpaRepository<Turma, Integer> {
    
}