package com.forms.service;

import com.forms.models.Perfil;
import com.forms.models.Usuario;
import com.forms.repository.PerfilRepository;
import com.forms.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Cadastra um novo usuário com senha hasheada
     */
    @Transactional
    public Usuario cadastrarUsuario(Usuario usuario, String senhaPlana) {
        // Validar se email já existe
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado no sistema");
        }

        // Hash da senha
        String senhaHash = passwordEncoder.encode(senhaPlana);
        usuario.setSenhaHash(senhaHash);

        // Validar se perfil existe
        if (usuario.getPerfil() == null || usuario.getPerfil().getId() == null) {
            throw new IllegalArgumentException("Perfil é obrigatório");
        }

        Perfil perfil = perfilRepository.findById(usuario.getPerfil().getId())
                .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado"));

        usuario.setPerfil(perfil);

        return usuarioRepository.save(usuario);
    }

    /**
     * Busca usuário por email
     */
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Busca usuário por ID
     */
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Lista todos os usuários
     */
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Atualiza informações do usuário (exceto senha)
     */
    @Transactional
    public Usuario atualizar(Usuario usuario) {
        if (!usuarioRepository.existsById(usuario.getId())) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        return usuarioRepository.save(usuario);
    }

    /**
     * Atualiza senha do usuário
     */
    @Transactional
    public void atualizarSenha(Integer usuarioId, String novaSenhaPlana) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        String senhaHash = passwordEncoder.encode(novaSenhaPlana);
        usuario.setSenhaHash(senhaHash);

        usuarioRepository.save(usuario);
    }

    /**
     * Verifica se senha está correta
     */
    public boolean verificarSenha(Usuario usuario, String senhaPlana) {
        return passwordEncoder.matches(senhaPlana, usuario.getSenhaHash());
    }

    /**
     * Deleta usuário por ID
     */
    @Transactional
    public void deletar(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    // Busca um usuário com um perfil específico por nome
    public Optional<Usuario> buscarPorPerfilNome(String nomePerfil) {
        return perfilRepository.findByNome(nomePerfil).flatMap(perfil -> {
            return Optional.empty(); 
        });
    }

    // Lista todos os usuários de um perfil (usado para popular selects)
    public List<Usuario> listarPorPerfil(Perfil perfil) {
        return usuarioRepository.findAll().stream()
               .filter(u -> u.getPerfil().getId().equals(perfil.getId()))
               .collect(java.util.stream.Collectors.toList());
    }
}
