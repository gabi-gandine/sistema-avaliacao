package com.forms.repository;

import com.forms.models.LogAuditoria;
import com.forms.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para LogAuditoria
 * RNF04: Queries para consultar logs de auditoria
 */
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {

    /**
     * Busca logs de um usuário específico
     */
    List<LogAuditoria> findByUsuario(Usuario usuario);

    /**
     * Busca logs de um usuário ordenados por timestamp (mais recentes primeiro)
     */
    @Query("SELECT l FROM LogAuditoria l WHERE l.usuario.id = :usuarioId ORDER BY l.timestamp DESC")
    List<LogAuditoria> findByUsuarioIdOrderByTimestampDesc(@Param("usuarioId") Integer usuarioId);

    /**
     * Busca logs por ação específica
     */
    List<LogAuditoria> findByAcao(String acao);

    /**
     * Busca logs por tipo
     */
    List<LogAuditoria> findByTipo(String tipo);

    /**
     * Busca logs por resultado (SUCESSO, FALHA, NEGADO)
     */
    List<LogAuditoria> findByResultado(String resultado);

    /**
     * Busca logs de falhas (para detecção de problemas)
     */
    @Query("SELECT l FROM LogAuditoria l WHERE l.resultado = 'FALHA' ORDER BY l.timestamp DESC")
    List<LogAuditoria> findFalhas();

    /**
     * Busca logs de acessos negados (tentativas de acesso não autorizado)
     */
    @Query("SELECT l FROM LogAuditoria l WHERE l.resultado = 'NEGADO' ORDER BY l.timestamp DESC")
    List<LogAuditoria> findAcessosNegados();

    /**
     * Busca logs em um período específico
     */
    @Query("SELECT l FROM LogAuditoria l WHERE l.timestamp BETWEEN :inicio AND :fim ORDER BY l.timestamp DESC")
    List<LogAuditoria> findByPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    /**
     * Busca logs de um usuário em um período
     */
    @Query("SELECT l FROM LogAuditoria l WHERE l.usuario.id = :usuarioId " +
           "AND l.timestamp BETWEEN :inicio AND :fim ORDER BY l.timestamp DESC")
    List<LogAuditoria> findByUsuarioAndPeriodo(
            @Param("usuarioId") Integer usuarioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    /**
     * Busca logs por IP (para detecção de fraudes ou múltiplos acessos)
     */
    List<LogAuditoria> findByIpAddress(String ipAddress);

    /**
     * Conta ações de um usuário em um período (para detecção de comportamento anormal)
     */
    @Query("SELECT COUNT(l) FROM LogAuditoria l WHERE l.usuario.id = :usuarioId " +
           "AND l.timestamp BETWEEN :inicio AND :fim")
    Long countAcoesPorUsuarioNoPeriodo(
            @Param("usuarioId") Integer usuarioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    /**
     * Busca últimas N ações de um usuário
     */
    @Query(value = "SELECT l FROM LogAuditoria l WHERE l.usuario.id = :usuarioId " +
           "ORDER BY l.timestamp DESC")
    List<LogAuditoria> findUltimasAcoesUsuario(@Param("usuarioId") Integer usuarioId);

    /**
     * Busca logs de login/logout para auditoria de sessões
     */
    @Query("SELECT l FROM LogAuditoria l WHERE l.acao IN ('LOGIN', 'LOGOUT') " +
           "ORDER BY l.timestamp DESC")
    List<LogAuditoria> findLogsAutenticacao();

    /**
     * Busca logs de submissões de formulários
     */
    @Query("SELECT l FROM LogAuditoria l WHERE l.acao = 'SUBMETER_RESPOSTA' " +
           "ORDER BY l.timestamp DESC")
    List<LogAuditoria> findLogsSubmissoes();

    /**
     * Busca logs de acesso a relatórios (para rastrear quem viu o quê)
     */
    @Query("SELECT l FROM LogAuditoria l WHERE l.tipo = 'RELATORIO' " +
           "ORDER BY l.timestamp DESC")
    List<LogAuditoria> findLogsRelatorios();

    /**
     * Busca logs mais recentes (para painel de auditoria)
     */
    @Query("SELECT l FROM LogAuditoria l ORDER BY l.timestamp DESC")
    List<LogAuditoria> findRecentLogs();

    /**
     * Busca tentativas de login falhas (segurança)
     */
    @Query("SELECT l FROM LogAuditoria l WHERE l.acao = 'LOGIN' AND l.resultado = 'FALHA' " +
           "ORDER BY l.timestamp DESC")
    List<LogAuditoria> findLoginsFalhos();

    /**
     * Conta logins falhos de um IP em um período (detecção de ataques)
     */
    @Query("SELECT COUNT(l) FROM LogAuditoria l WHERE l.acao = 'LOGIN' " +
           "AND l.resultado = 'FALHA' AND l.ipAddress = :ip " +
           "AND l.timestamp > :dataLimite")
    Long countLoginsFalhosPorIp(
            @Param("ip") String ip,
            @Param("dataLimite") LocalDateTime dataLimite
    );
}
