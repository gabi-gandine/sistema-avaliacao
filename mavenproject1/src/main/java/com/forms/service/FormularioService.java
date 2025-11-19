package com.forms.service;

import com.forms.models.*;
import com.forms.repository.FormularioRepository;
import com.forms.repository.SubmissaoControleRepository;
import com.forms.repository.PerfilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service para gerenciar Formulários de Avaliação (RF07, RF11, RF12)
 *
 * Implementa:
 * - RF07: Formulários com perfis destinatários
 * - RF11: Formulários anônimos ou identificados
 * - RF12: Controle de acesso por turma/perfil
 *
 * @author gabriela
 */
@Service
public class FormularioService {

    @Autowired
    private FormularioRepository formularioRepository;

    @Autowired
    private SubmissaoControleRepository submissaoRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private com.forms.repository.UsuarioRepository usuarioRepository;

    /**
     * RF07: Cria um formulário com perfis destinatários
     *
     * @param formulario Formulário a ser criado
     * @param processo Processo avaliativo ao qual pertence
     * @param perfisDestino Perfis que podem responder (pode ser vazio = todos)
     * @param criador Usuário que está criando
     * @return Formulário criado
     */
    @Transactional
    public Formulario criar(Formulario formulario, ProcessoAvaliativo processo,
                           Set<Perfil> perfisDestino, Usuario criador) {
        if (formulario.getTitulo() == null || formulario.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("Título do formulário é obrigatório");
        }

        formulario.setProcessoAvaliativo(processo);
        formulario.setCriador(criador);
        formulario.setAtivo(true);

        // RF07: Define perfis destinatários (se vazio, qualquer perfil pode responder)
        if (perfisDestino != null && !perfisDestino.isEmpty()) {
            formulario.setPerfisDestino(perfisDestino);
        }

        return formularioRepository.save(formulario);
    }

    /**
     * Atualiza um formulário existente
     */
    @Transactional
    public Formulario atualizar(Formulario formulario) {
        if (formulario.getId() == null) {
            throw new IllegalArgumentException("ID do formulário é obrigatório para atualização");
        }

        if (!formularioRepository.existsById(formulario.getId())) {
            throw new IllegalArgumentException("Formulário não encontrado");
        }

        // Validar datas se definidas
        if (formulario.getDataInicio() != null && formulario.getDataFim() != null) {
            if (formulario.getDataFim().isBefore(formulario.getDataInicio())) {
                throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
            }
        }

        return formularioRepository.save(formulario);
    }

    /**
     * Busca um formulário por ID
     */
    public Optional<Formulario> buscarPorId(Integer id) {
        return formularioRepository.findById(id);
    }

    /**
     * Lista todos os formulários
     */
    public List<Formulario> listarTodos() {
        return formularioRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Lista formulários de um processo avaliativo específico
     */
    public List<Formulario> listarPorProcesso(ProcessoAvaliativo processo) {
        return formularioRepository.findByProcessoAvaliativo(processo);
    }

    /**
     * Lista formulários criados por um usuário
     */
    public List<Formulario> listarPorCriador(Usuario criador) {
        return formularioRepository.findByCriador(criador);
    }

    /**
     * RF07: Lista formulários destinados a um perfil específico
     *
     * Retorna formulários que:
     * - Têm o perfil na lista de destinatários OU
     * - Não têm restrição de perfil
     *
     * @param perfil Perfil do usuário
     * @return Lista de formulários disponíveis para este perfil
     */
    public List<Formulario> listarPorPerfilDestino(Perfil perfil) {
        return formularioRepository.findByPerfilDestino(perfil.getId());
    }

    /**
     * RF12: Lista formulários disponíveis para um usuário específico
     *
     * Considera:
     * - Perfil do usuário
     * - Período ativo do formulário
     * - Status ativo do formulário
     *
     * @param usuario Usuário logado
     * @return Lista de formulários disponíveis
     */
    public List<Formulario> listarDisponiveisParaUsuario(Usuario usuario) {
        return formularioRepository.findFormulariosDisponiveisParaUsuario(
                usuario.getPerfil().getId(),
                LocalDateTime.now()
        );
    }

    /**
     * Lista formulários no período ativo (entre dataInicio e dataFim)
     */
    public List<Formulario> listarNoPeriodo() {
        return formularioRepository.findFormulariosNoPeriodo(LocalDateTime.now());
    }

    /**
     * Lista formulários ativos de um processo
     */
    public List<Formulario> listarAtivosPorProcesso(Integer processoId) {
        return formularioRepository.findFormulariosAtivosPorProcesso(processoId);
    }

    /**
     * Busca formulários por título (busca parcial)
     */
    public List<Formulario> buscarPorTitulo(String titulo) {
        return formularioRepository.findByTituloContaining(titulo);
    }

    /**
     * RF07: Verifica se um usuário pode acessar um formulário
     *
     * @param formulario Formulário a verificar
     * @param usuario Usuário tentando acessar
     * @return true se o usuário pode acessar
     */
    public boolean usuarioPodeAcessar(Formulario formulario, Usuario usuario) {
        // Verifica se o formulário está ativo
        if (!formulario.getAtivo()) {
            return false;
        }

        // Verifica período (se definido)
        if (!formulario.isNoPeriodo()) {
            return false;
        }

        // RF07: Verifica se o perfil pode responder
        return formulario.perfilPodeResponder(usuario.getPerfil());
    }

    /**
     * RF13: Verifica se um usuário já respondeu um formulário
     *
     * @param formularioId ID do formulário
     * @param usuarioId ID do usuário
     * @return true se já respondeu
     */
    public boolean usuarioJaRespondeu(Integer formularioId, Integer usuarioId) {
        return submissaoRepository.jaRespondeu(formularioId, usuarioId);
    }

    /**
     * Adiciona um perfil destinatário a um formulário
     */
    @Transactional
    public Formulario adicionarPerfilDestino(Integer formularioId, Perfil perfil) {
        Formulario formulario = formularioRepository.findById(formularioId)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        formulario.adicionarPerfilDestino(perfil);
        return formularioRepository.save(formulario);
    }

    /**
     * Ativa um formulário
     */
    @Transactional
    public Formulario ativar(Integer id) {
        Formulario formulario = formularioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        formulario.setAtivo(true);
        return formularioRepository.save(formulario);
    }

    /**
     * Desativa um formulário (não deleta, apenas marca como inativo)
     */
    @Transactional
    public Formulario desativar(Integer id) {
        Formulario formulario = formularioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        formulario.setAtivo(false);
        return formularioRepository.save(formulario);
    }

    /**
     * Deleta um formulário
     * CUIDADO: Isso deletará em cascata todas as questões, respostas e submissões!
     */
    @Transactional
    public void deletar(Integer id) {
        if (!formularioRepository.existsById(id)) {
            throw new IllegalArgumentException("Formulário não encontrado");
        }

        formularioRepository.deleteById(id);
    }

    /**
     * Verifica se um formulário está no período ativo
     */
    public boolean estaNoPeriodo(Integer id) {
        Formulario formulario = formularioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        return formulario.isNoPeriodo();
    }

    /**
     * RF11: Verifica se um formulário é anônimo
     */
    public boolean isAnonimo(Integer id) {
        Formulario formulario = formularioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        return formulario.getIsAnonimo();
    }

    /**
     * Conta quantas submissões um formulário recebeu
     */
    public Long contarSubmissoes(Integer formularioId) {
        return submissaoRepository.countSubmissoesCompletas(formularioId);
    }

    // ========== MÉTODOS ADICIONAIS PARA CONTROLLERS ==========

    /**
     * Alias para listarPorCriador (compatibilidade com controller)
     */
    public List<Formulario> buscarPorCriador(Usuario criador) {
        return listarPorCriador(criador);
    }

    /**
     * RF07: Cria formulário (assinatura simplificada para controller)
     */
    @Transactional
    public Formulario criar(String titulo, String descricao, Usuario criador) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("Título do formulário é obrigatório");
        }

        Formulario formulario = new Formulario();
        formulario.setTitulo(titulo);
        formulario.setDescricao(descricao);
        formulario.setCriador(criador);
        formulario.setAtivo(true);
        formulario.setIsAnonimo(false);
        formulario.setPermiteEdicao(false);

        return formularioRepository.save(formulario);
    }

    /**
     * Atualiza formulário (assinatura simplificada para controller)
     */
    @Transactional
    public Formulario atualizar(Integer id, String titulo, String descricao) {
        Formulario formulario = formularioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        if (titulo != null && !titulo.trim().isEmpty()) {
            formulario.setTitulo(titulo);
        }

        if (descricao != null) {
            formulario.setDescricao(descricao);
        }

        return formularioRepository.save(formulario);
    }

    /**
     * RF11: Define se formulário é anônimo
     */
    @Transactional
    public Formulario definirAnonimato(Integer id, Boolean isAnonimo) {
        Formulario formulario = formularioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        formulario.setIsAnonimo(isAnonimo);
        return formularioRepository.save(formulario);
    }

    /**
     * RF12: Habilita edição de respostas
     */
    @Transactional
    public Formulario habilitarEdicao(Integer id) {
        Formulario formulario = formularioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        formulario.setPermiteEdicao(true);
        return formularioRepository.save(formulario);
    }

    /**
     * RF12: Desabilita edição de respostas
     */
    @Transactional
    public Formulario desabilitarEdicao(Integer id) {
        Formulario formulario = formularioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        formulario.setPermiteEdicao(false);
        return formularioRepository.save(formulario);
    }

    /**
     * RF07: Lista formulários disponíveis para um perfil (alias para compatibilidade)
     */
    public List<Formulario> listarDisponiveisParaPerfil(Perfil perfil) {
        return listarPorPerfilDestino(perfil);
    }

    /**
     * RF07: Define perfis destinatários a partir de lista de IDs
     */
    @Transactional
    public Formulario definirPerfisDestino(Integer formularioId, Set<Integer> perfisIds) {
        Formulario formulario = formularioRepository.findById(formularioId)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        // Se perfisIds está vazio, remove todos os perfis (qualquer um pode responder)
        if (perfisIds == null || perfisIds.isEmpty()) {
            formulario.getPerfisDestino().clear();
            return formularioRepository.save(formulario);
        }

        // Busca e adiciona perfis
        formulario.getPerfisDestino().clear();
        for (Integer perfilId : perfisIds) {
            Perfil perfil = perfilRepository.findById(perfilId)
                    .orElseThrow(() -> new IllegalArgumentException("Perfil não encontrado: " + perfilId));
            formulario.getPerfisDestino().add(perfil);
        }

        return formularioRepository.save(formulario);
    }

    /**
     * RF12: Lista formulários disponíveis para um aluno específico (por ID)
     * Simplificação para controllers MVC
     */
    public List<Formulario> listarDisponiveisParaAluno(Integer alunoId) {
        Usuario usuario = buscarUsuarioPorId(alunoId);
        return listarDisponiveisParaUsuario(usuario);
    }

    /**
     * RF12: Verifica se um aluno tem acesso a um formulário (por IDs)
     * Simplificação para controllers MVC
     */
    public boolean alunoTemAcesso(Integer alunoId, Integer formularioId) {
        Usuario usuario = buscarUsuarioPorId(alunoId);
        Formulario formulario = buscarPorId(formularioId)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));
        return usuarioPodeAcessar(formulario, usuario);
    }

    /**
     * Helper method para buscar usuário por ID
     */
    private Usuario buscarUsuarioPorId(Integer usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }
}
