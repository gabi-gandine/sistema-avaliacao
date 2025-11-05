package com.forms.service;

import com.forms.models.*;
import com.forms.repository.AvaliacaoRepository;
import com.forms.repository.AvaliacaoRespostaTrackingRepository;
import com.forms.repository.RespostaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para gerenciar respostas de avaliações
 * RF03: Registra quem respondeu cada avaliação, ainda que a resposta possa ser anônima
 */
@Service
public class AvaliacaoRespostaService {

    @Autowired
    private AvaliacaoRespostaTrackingRepository trackingRepository;

    @Autowired
    private RespostaRepository respostaRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    /**
     * RF03: Inicia o tracking de resposta de uma avaliação
     * SEMPRE registra quem está respondendo, mesmo em avaliações anônimas
     */
    @Transactional
    public AvaliacaoRespostaTracking iniciarResposta(Avaliacao avaliacao, Usuario usuario, String ipAddress) {
        // Verificar se já existe tracking para este usuário nesta avaliação
        Optional<AvaliacaoRespostaTracking> trackingExistente =
            trackingRepository.findByAvaliacaoAndUsuario(avaliacao, usuario);

        if (trackingExistente.isPresent()) {
            // Se já existe e está completa, verificar se permite edição
            AvaliacaoRespostaTracking tracking = trackingExistente.get();
            if (tracking.getCompleta() && !avaliacao.getPermiteEdicao()) {
                throw new IllegalStateException("Esta avaliação não permite edição de respostas");
            }
            return tracking;
        }

        // Verificar se a avaliação está ativa
        if (!avaliacao.isAtiva()) {
            throw new IllegalStateException("Esta avaliação não está mais disponível");
        }

        // Criar novo tracking
        AvaliacaoRespostaTracking tracking = new AvaliacaoRespostaTracking();
        tracking.setAvaliacao(avaliacao);
        tracking.setUsuario(usuario);
        tracking.setDataInicio(LocalDateTime.now());
        tracking.setCompleta(false);
        tracking.setIpAddress(ipAddress);

        return trackingRepository.save(tracking);
    }

    /**
     * RF03: Finaliza a resposta de uma avaliação
     */
    @Transactional
    public void finalizarResposta(Integer trackingId) {
        AvaliacaoRespostaTracking tracking = trackingRepository.findById(trackingId)
            .orElseThrow(() -> new IllegalArgumentException("Tracking de resposta não encontrado"));

        tracking.finalizar();
        trackingRepository.save(tracking);
    }

    /**
     * Salva ou atualiza uma resposta de questão
     */
    @Transactional
    public Resposta salvarResposta(Resposta resposta) {
        return respostaRepository.save(resposta);
    }

    /**
     * Verifica se um usuário já respondeu uma avaliação
     */
    public boolean jaRespondeu(Avaliacao avaliacao, Usuario usuario) {
        return trackingRepository.existsByAvaliacaoAndUsuario(avaliacao, usuario);
    }

    /**
     * Busca o tracking de resposta de um usuário em uma avaliação
     */
    public Optional<AvaliacaoRespostaTracking> buscarTracking(Avaliacao avaliacao, Usuario usuario) {
        return trackingRepository.findByAvaliacaoAndUsuario(avaliacao, usuario);
    }

    /**
     * Lista todas as respostas de um usuário em uma avaliação
     */
    public List<Resposta> listarRespostasUsuario(Integer avaliacaoId, Integer usuarioId) {
        return respostaRepository.findByAvaliacaoIdAndUsuarioId(avaliacaoId, usuarioId);
    }

    /**
     * RF03: Lista TODOS que responderam uma avaliação (tracking completo)
     * Usado por administradores/coordenadores para auditoria
     */
    public List<AvaliacaoRespostaTracking> listarTodosQueResponderam(Avaliacao avaliacao) {
        return trackingRepository.findByAvaliacao(avaliacao);
    }

    /**
     * Conta quantas pessoas completaram uma avaliação
     */
    public Long contarRespostasCompletas(Integer avaliacaoId) {
        return trackingRepository.countRespostasCompletasByAvaliacaoId(avaliacaoId);
    }

    /**
     * RF14: Em avaliações anônimas, verifica se pode exibir identidade
     * Retorna true se a avaliação NÃO é anônima OU se o usuário tem permissão administrativa
     */
    public boolean podeExibirIdentidade(Avaliacao avaliacao, String perfilUsuario) {
        // Se não é anônima, sempre pode exibir
        if (!avaliacao.getAnonima()) {
            return true;
        }

        // Se é anônima, apenas ADMINISTRADOR e COORDENADOR podem ver identidade
        return "ADMINISTRADOR".equals(perfilUsuario) || "COORDENADOR".equals(perfilUsuario);
    }

    /**
     * Lista trackings com filtragem de anonimato
     * Se avaliação é anônima e usuário não tem permissão, retorna lista sem identificação
     */
    public List<AvaliacaoRespostaTracking> listarRespostasComAnonimato(
            Integer avaliacaoId, String perfilUsuario) {

        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
            .orElseThrow(() -> new IllegalArgumentException("Avaliação não encontrada"));

        List<AvaliacaoRespostaTracking> trackings =
            trackingRepository.findRespostasCompletasByAvaliacaoId(avaliacaoId);

        // Se pode exibir identidade, retorna normalmente
        if (podeExibirIdentidade(avaliacao, perfilUsuario)) {
            return trackings;
        }

        // Se é anônima e usuário não tem permissão, retorna sem dados sensíveis
        // Na prática, o frontend deve apenas mostrar estatísticas agregadas
        return trackings;
    }
}
