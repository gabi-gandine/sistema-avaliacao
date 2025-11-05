package com.forms.service;

import com.forms.models.Perfil;
import com.forms.repository.PerfilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PerfilService {

    @Autowired
    private PerfilRepository perfilRepository;

    /**
     * Lista todos os perfis disponíveis
     */
    public List<Perfil> listarTodos() {
        return perfilRepository.findAll();
    }

    /**
     * Busca perfil por ID
     */
    public Optional<Perfil> buscarPorId(Integer id) {
        return perfilRepository.findById(id);
    }

    /**
     * Busca perfil por nome (ex: "ALUNO", "PROFESSOR")
     */
    public Optional<Perfil> buscarPorNome(String nome) {
        return perfilRepository.findByNome(nome);
    }

    /**
     * Verifica se perfil existe por nome
     */
    public boolean existePorNome(String nome) {
        return perfilRepository.findByNome(nome).isPresent();
    }
}
