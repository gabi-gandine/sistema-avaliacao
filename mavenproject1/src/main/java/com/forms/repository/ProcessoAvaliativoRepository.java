package com.forms.repository;

import com.forms.models.ProcessoAvaliativo;
import com.forms.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para ProcessoAvaliativo
 * Fornece queries customizadas para buscar processos avaliativos
 */
public interface ProcessoAvaliativoRepository extends JpaRepository<ProcessoAvaliativo, Integer> {

    /**
     * Busca processos avaliativos criados por um usuário específico
     */
    List<ProcessoAvaliativo> findByCriador(Usuario criador);

    /**
     * Busca apenas processos avaliativos ativos
     */
    List<ProcessoAvaliativo> findByAtivoTrue();

    /**
     * Busca processos avaliativos que estão no período ativo (entre data início e fim)
     * e que estão marcados como ativos
     */
    @Query("SELECT p FROM ProcessoAvaliativo p WHERE p.ativo = true AND p.dataInicio <= :agora AND p.dataFim >= :agora")
    List<ProcessoAvaliativo> findProcessosNoPeriodo(@Param("agora") LocalDateTime agora);

    /**
     * Busca processos avaliativos que se sobrepõem com um período específico
     * Sobrecarga do método acima para aceitar range de datas
     */
    @Query("SELECT p FROM ProcessoAvaliativo p WHERE p.ativo = true AND p.dataInicio <= :fim AND p.dataFim >= :inicio")
    List<ProcessoAvaliativo> findProcessosNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    /**
     * Busca processos avaliativos futuros (ainda não iniciados)
     */
    @Query("SELECT p FROM ProcessoAvaliativo p WHERE p.ativo = true AND p.dataInicio > :agora ORDER BY p.dataInicio ASC")
    List<ProcessoAvaliativo> findProcessosFuturos(@Param("agora") LocalDateTime agora);

    /**
     * Busca processos avaliativos encerrados
     */
    @Query("SELECT p FROM ProcessoAvaliativo p WHERE p.dataFim < :agora ORDER BY p.dataFim DESC")
    List<ProcessoAvaliativo> findProcessosEncerrados(@Param("agora") LocalDateTime agora);

    /**
     * Busca processos por nome (busca parcial, case-insensitive)
     */
    @Query("SELECT p FROM ProcessoAvaliativo p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<ProcessoAvaliativo> findByNomeContaining(@Param("nome") String nome);

    /**
     * Busca todos os processos ordenados por data de início (mais recentes primeiro)
     */
    List<ProcessoAvaliativo> findAllByOrderByDataInicioDesc();
}
