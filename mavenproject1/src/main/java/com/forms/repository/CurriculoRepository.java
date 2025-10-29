package com.forms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forms.models.Curriculo;

public interface CurriculoRepository extends JpaRepository<Curriculo, Integer> {

    
}