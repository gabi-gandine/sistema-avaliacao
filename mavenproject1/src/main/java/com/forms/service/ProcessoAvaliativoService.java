package com.forms.service;

import com.forms.models.ProcessoAvaliativo;
import com.forms.models.Formulario;
import com.forms.models.Usuario;
import com.forms.repository.ProcessoAvaliativoRepository;
import com.forms.repository.FormularioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciar Processos Avaliativos (RF05)
 *
 * Um Processo Avaliativo agrupa múltiplos formulários de avaliação
 * e define o período em que as avaliações estarão disponíveis.
 *
 * Exemplo: "Avaliação Institucional 2025.2" contém:
 * - Formulário de Avaliação Docente (para alunos)
 * - Formulário de Autoavaliação (para professores)
 * - Formulário de Avaliação de Infraestrutura (para todos)
 *
 * @author gabriela
 */
@Service
public class ProcessoAvaliativoService {

    @Autowired
    private ProcessoAvaliativoRepository processoRepository;

    @Autowired
    private FormularioRepository formularioRepository;

    /**
     * RF05: Cria um novo processo avaliativo
     *
     * @param processo Processo a ser criado
     * @param criador Usuário que está criando (coordenador/admin)
     * @return Processo criado
     */
    @Transactional
    public ProcessoAvaliativo criar(ProcessoAvaliativo processo, Usuario criador) {
        if (processo.getNome() == null || processo.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do processo é obrigatório");
        }

        if (processo.getDataInicio() == null) {
            throw new IllegalArgumentException("Data de início é obrigatória");
        }

        if (processo.getDataFim() == null) {
            throw new IllegalArgumentException("Data de fim é obrigatória");
        }

        if (processo.getDataFim().isBefore(processo.getDataInicio())) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }

        processo.setCriador(criador);
        processo.setAtivo(true);

        return processoRepository.save(processo);
    }

    /**
     * Atualiza um processo avaliativo existente
     */
    @Transactional
    public ProcessoAvaliativo atualizar(ProcessoAvaliativo processo) {
        if (processo.getId() == null) {
            throw new IllegalArgumentException("ID do processo é obrigatório para atualização");
        }

        if (!processoRepository.existsById(processo.getId())) {
            throw new IllegalArgumentException("Processo avaliativo não encontrado");
        }

        if (processo.getDataFim().isBefore(processo.getDataInicio())) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }

        return processoRepository.save(processo);
    }

    /**
     * Busca um processo avaliativo por ID
     */
    public Optional<ProcessoAvaliativo> buscarPorId(Integer id) {
        return processoRepository.findById(id);
    }

    /**
     * Lista todos os processos avaliativos (ordenados por data de início)
     */
    public List<ProcessoAvaliativo> listarTodos() {
        return processoRepository.findAllByOrderByDataInicioDesc();
    }

    /**
     * Lista apenas processos avaliativos ativos
     */
    public List<ProcessoAvaliativo> listarAtivos() {
        return processoRepository.findByAtivoTrue();
    }

    /**
     * Lista processos avaliativos que estão no período ativo
     * (data atual entre data de início e fim)
     */
    public List<ProcessoAvaliativo> listarNoPeriodo() {
        return processoRepository.findProcessosNoPeriodo(LocalDateTime.now());
    }

    /**
     * Lista processos avaliativos futuros (ainda não iniciados)
     */
    public List<ProcessoAvaliativo> listarFuturos() {
        return processoRepository.findProcessosFuturos(LocalDateTime.now());
    }

    /**
     * Lista processos avaliativos encerrados
     */
    public List<ProcessoAvaliativo> listarEncerrados() {
        return processoRepository.findProcessosEncerrados(LocalDateTime.now());
    }

    /**
     * Busca processos avaliativos criados por um usuário específico
     */
    public List<ProcessoAvaliativo> listarPorCriador(Usuario criador) {
        return processoRepository.findByCriador(criador);
    }

    /**
     * Busca processos por nome (busca parcial)
     */
    public List<ProcessoAvaliativo> buscarPorNome(String nome) {
        return processoRepository.findByNomeContaining(nome);
    }

    /**
     * RF05: Vincula um formulário a um processo avaliativo
     *
     * @param processoId ID do processo
     * @param formulario Formulário a ser vinculado
     */
    @Transactional
    public ProcessoAvaliativo vincularFormulario(Integer processoId, Formulario formulario) {
        ProcessoAvaliativo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new IllegalArgumentException("Processo avaliativo não encontrado"));

        processo.adicionarFormulario(formulario);

        return processoRepository.save(processo);
    }

    /**
     * Ativa um processo avaliativo
     */
    @Transactional
    public ProcessoAvaliativo ativar(Integer id) {
        ProcessoAvaliativo processo = processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo avaliativo não encontrado"));

        processo.setAtivo(true);
        return processoRepository.save(processo);
    }

    /**
     * Desativa um processo avaliativo (não deleta, apenas marca como inativo)
     */
    @Transactional
    public ProcessoAvaliativo desativar(Integer id) {
        ProcessoAvaliativo processo = processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo avaliativo não encontrado"));

        processo.setAtivo(false);
        return processoRepository.save(processo);
    }

    /**
     * Deleta um processo avaliativo
     * CUIDADO: Isso deletará em cascata todos os formulários vinculados!
     */
    @Transactional
    public void deletar(Integer id) {
        if (!processoRepository.existsById(id)) {
            throw new IllegalArgumentException("Processo avaliativo não encontrado");
        }

        processoRepository.deleteById(id);
    }

    /**
     * Verifica se um processo está no período ativo
     */
    public boolean estaNoPeriodo(Integer id) {
        ProcessoAvaliativo processo = processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo avaliativo não encontrado"));

        return processo.isNoPeriodo();
    }

    // ========== MÉTODOS ADICIONAIS PARA CONTROLLERS ==========

    /**
     * RF05: Cria processo com parâmetros simples (sobrecarga para controller)
     */
    @Transactional
    public ProcessoAvaliativo criar(String nome, String descricao, LocalDateTime dataInicio, LocalDateTime dataFim) {
        ProcessoAvaliativo processo = new ProcessoAvaliativo();
        processo.setNome(nome);
        processo.setDescricao(descricao);
        processo.setDataInicio(dataInicio);
        processo.setDataFim(dataFim);
        processo.setAtivo(true);

        // Valida datas
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }

        return processoRepository.save(processo);
    }

    /**
     * RF05: Atualiza processo com parâmetros simples (sobrecarga para controller)
     */
    @Transactional
    public ProcessoAvaliativo atualizar(Integer id, String nome, String descricao,
                                       LocalDateTime dataInicio, LocalDateTime dataFim) {
        ProcessoAvaliativo processo = processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo avaliativo não encontrado"));

        if (nome != null && !nome.trim().isEmpty()) {
            processo.setNome(nome);
        }

        if (descricao != null) {
            processo.setDescricao(descricao);
        }

        if (dataInicio != null) {
            processo.setDataInicio(dataInicio);
        }

        if (dataFim != null) {
            processo.setDataFim(dataFim);
        }

        // Valida datas se ambas foram definidas
        if (processo.getDataInicio() != null && processo.getDataFim() != null) {
            if (processo.getDataFim().isBefore(processo.getDataInicio())) {
                throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
            }
        }

        return processoRepository.save(processo);
    }

    /**
     * RF05: Vincula formulário por ID (sobrecarga para controller)
     */
    @Transactional
    public ProcessoAvaliativo vincularFormulario(Integer processoId, Integer formularioId) {
        ProcessoAvaliativo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new IllegalArgumentException("Processo avaliativo não encontrado"));

        Formulario formulario = formularioRepository.findById(formularioId)
                .orElseThrow(() -> new IllegalArgumentException("Formulário não encontrado"));

        // Adiciona o formulário ao processo se ainda não estiver vinculado
        if (!processo.getFormularios().contains(formulario)) {
            processo.getFormularios().add(formulario);
        }

        return processoRepository.save(processo);
    }

    /**
     * RF05: Desvincula formulário de um processo
     */
    @Transactional
    public ProcessoAvaliativo desvincularFormulario(Integer processoId, Integer formularioId) {
        ProcessoAvaliativo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new IllegalArgumentException("Processo avaliativo não encontrado"));

        // Remove o formulário do processo
        processo.getFormularios().removeIf(f -> f.getId().equals(formularioId));

        return processoRepository.save(processo);
    }

    /**
     * Busca processos por período
     */
    public List<ProcessoAvaliativo> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return processoRepository.findProcessosNoPeriodo(inicio, fim);
    }
}
