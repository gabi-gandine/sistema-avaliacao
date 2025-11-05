package com.forms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forms.models.Perfil;

import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Integer>{

    Optional<Perfil> findByNome(String nome);

}
