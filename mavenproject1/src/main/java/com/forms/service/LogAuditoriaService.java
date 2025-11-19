package com.forms.service;

import com.forms.models.LogAuditoria;
import com.forms.models.Usuario;
import com.forms.repository.LogAuditoriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service para gerenciar Logs de Auditoria (RNF04)
 *
 * Registra todas as ações importantes do sistema:
 * - Login/Logout
 * - Criação/edição de formulários
 * - Submissão de respostas
 * - Acesso a relatórios
 * - Tentativas de acesso negado
 * - Alterações administrativas
 *
 * @author gabriela
 */
@Service
public class LogAuditoriaService {

    @Autowired
    private LogAuditoriaRepository logRepository;

    /**
     * RNF04: Registra uma ação no log de auditoria
     *
     * @param usuario Usuário que executou a ação (pode ser null para ações anônimas)
     * @param acao Nome da ação (ex: "LOGIN", "CRIAR_FORMULARIO")
     * @param descricao Descrição detalhada da ação
     * @param tipo Tipo/categoria da ação (ex: "AUTENTICACAO", "FORMULARIO")
     * @param request HttpServletRequest para capturar IP e User-Agent
     * @param resultado Resultado da ação ("SUCESSO", "FALHA", "NEGADO")
     * @param detalhes Detalhes adicionais em formato JSON (opcional)
     */
    @Transactional
    public LogAuditoria registrar(Usuario usuario, String acao, String descricao,
                                  String tipo, HttpServletRequest request,
                                  String resultado, String detalhes) {
        LogAuditoria log = new LogAuditoria();
        log.setUsuario(usuario);
        log.setAcao(acao);
        log.setDescricao(descricao);
        log.setTipo(tipo);
        log.setResultado(resultado);
        log.setDetalhes(detalhes);

        // Captura IP e User-Agent
        if (request != null) {
            String ipAddress = getClientIpAddress(request);
            log.setIpAddress(ipAddress);
            log.setUserAgent(request.getHeader("User-Agent"));
        }

        return logRepository.save(log);
    }

    /**
     * Registra ação de sucesso
     */
    @Transactional
    public LogAuditoria registrarSucesso(Usuario usuario, String acao, String descricao,
                                        String tipo, HttpServletRequest request) {
        return registrar(usuario, acao, descricao, tipo, request, "SUCESSO", null);
    }

    /**
     * Registra ação de falha
     */
    @Transactional
    public LogAuditoria registrarFalha(Usuario usuario, String acao, String descricao,
                                      String tipo, HttpServletRequest request, String mensagemErro) {
        LogAuditoria log = registrar(usuario, acao, descricao, tipo, request, "FALHA", null);
        log.setMensagemErro(mensagemErro);
        return logRepository.save(log);
    }

    /**
     * Registra tentativa de acesso negado
     */
    @Transactional
    public LogAuditoria registrarAcessoNegado(Usuario usuario, String acao, String descricao,
                                             HttpServletRequest request) {
        return registrar(usuario, acao, descricao, "SEGURANCA", request, "NEGADO", null);
    }

    /**
     * Registra login bem-sucedido
     */
    @Transactional
    public LogAuditoria registrarLogin(Usuario usuario, HttpServletRequest request) {
        return registrarSucesso(usuario, "LOGIN",
                "Usuário " + usuario.getEmail() + " fez login",
                "AUTENTICACAO", request);
    }

    /**
     * Registra logout
     */
    @Transactional
    public LogAuditoria registrarLogout(Usuario usuario, HttpServletRequest request) {
        return registrarSucesso(usuario, "LOGOUT",
                "Usuário " + usuario.getEmail() + " fez logout",
                "AUTENTICACAO", request);
    }

    /**
     * Registra tentativa de login falha
     */
    @Transactional
    public LogAuditoria registrarLoginFalho(String email, HttpServletRequest request, String motivo) {
        return registrar(null, "LOGIN",
                "Tentativa de login falha para: " + email,
                "AUTENTICACAO", request, "FALHA", "Motivo: " + motivo);
    }

    /**
     * Registra criação de formulário
     */
    @Transactional
    public LogAuditoria registrarCriacaoFormulario(Usuario usuario, Integer formularioId,
                                                   String titulo, HttpServletRequest request) {
        return registrarSucesso(usuario, "CRIAR_FORMULARIO",
                "Formulário criado: " + titulo + " (ID: " + formularioId + ")",
                "FORMULARIO", request);
    }

    /**
     * Registra submissão de resposta
     */
    @Transactional
    public LogAuditoria registrarSubmissaoResposta(Usuario usuario, Integer formularioId,
                                                   String tituloFormulario, HttpServletRequest request) {
        return registrarSucesso(usuario, "SUBMETER_RESPOSTA",
                "Resposta submetida para: " + tituloFormulario + " (ID: " + formularioId + ")",
                "RESPOSTA", request);
    }

    /**
     * Registra acesso a relatório
     */
    @Transactional
    public LogAuditoria registrarAcessoRelatorio(Usuario usuario, Integer formularioId,
                                                 String nivel, HttpServletRequest request) {
        return registrarSucesso(usuario, "ACESSAR_RELATORIO",
                "Relatório acessado - Formulário ID: " + formularioId + " (Nível: " + nivel + ")",
                "RELATORIO", request);
    }

    /**
     * Busca logs de um usuário específico
     */
    public List<LogAuditoria> buscarPorUsuario(Usuario usuario) {
        return logRepository.findByUsuarioIdOrderByTimestampDesc(usuario.getId());
    }

    /**
     * Busca logs por período
     */
    public List<LogAuditoria> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return logRepository.findByPeriodo(inicio, fim);
    }

    /**
     * Busca logs de falhas
     */
    public List<LogAuditoria> buscarFalhas() {
        return logRepository.findFalhas();
    }

    /**
     * Busca logs de acessos negados
     */
    public List<LogAuditoria> buscarAcessosNegados() {
        return logRepository.findAcessosNegados();
    }

    /**
     * Busca logs de autenticação (login/logout)
     */
    public List<LogAuditoria> buscarLogsAutenticacao() {
        return logRepository.findLogsAutenticacao();
    }

    /**
     * Busca logs de submissões
     */
    public List<LogAuditoria> buscarLogsSubmissoes() {
        return logRepository.findLogsSubmissoes();
    }

    /**
     * Busca logs de acessos a relatórios
     */
    public List<LogAuditoria> buscarLogsRelatorios() {
        return logRepository.findLogsRelatorios();
    }

    /**
     * Busca logs mais recentes (para dashboard)
     */
    public List<LogAuditoria> buscarRecentes() {
        return logRepository.findRecentLogs();
    }

    /**
     * Busca tentativas de login falhas
     */
    public List<LogAuditoria> buscarLoginsFalhos() {
        return logRepository.findLoginsFalhos();
    }

    /**
     * Verifica se um IP está com muitas tentativas de login falhas
     * (detecção de ataque)
     */
    public boolean ipSuspeito(String ip) {
        // Últimas 30 minutos
        LocalDateTime dataLimite = LocalDateTime.now().minusMinutes(30);
        Long tentativas = logRepository.countLoginsFalhosPorIp(ip, dataLimite);

        // Mais de 5 tentativas falhas em 30 minutos = suspeito
        return tentativas > 5;
    }

    /**
     * Extrai o endereço IP real do cliente (considerando proxies)
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
