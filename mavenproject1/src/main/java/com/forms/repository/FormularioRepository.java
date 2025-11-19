package com.forms.repository;

import com.forms.models.Formulario;
import com.forms.models.ProcessoAvaliativo;
import com.forms.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para Formulario
 * Fornece queries customizadas especialmente para filtrar por perfil destinatário (RF07)
 */
public interface FormularioRepository extends JpaRepository<Formulario, Integer> {

    /**
     * Busca formulários de um processo avaliativo específico
     */
    List<Formulario> findByProcessoAvaliativo(ProcessoAvaliativo processo);

    /**
     * Busca formulários criados por um usuário específico
     */
    List<Formulario> findByCriador(Usuario criador);

    /**
     * Busca apenas formulários ativos
     */
    List<Formulario> findByAtivoTrue();

    /**
     * RF07: Busca formulários destinados a um perfil específico
     * Retorna formulários que:
     * - Têm o perfil na lista de destinatários OU
     * - Não têm restrição de perfil (perfisDestino vazio)
     */
    @Query("SELECT DISTINCT f FROM Formulario f " +
           "LEFT JOIN f.perfisDestino p " +
           "WHERE f.ativo = true AND (p.id = :perfilId OR SIZE(f.perfisDestino) = 0)")
    List<Formulario> findByPerfilDestino(@Param("perfilId") Integer perfilId);

    /**
     * Busca formulários no período ativo (entre dataInicio e dataFim)
     */
    @Query("SELECT f FROM Formulario f WHERE f.ativo = true AND " +
           "(f.dataInicio IS NULL OR f.dataInicio <= :agora) AND " +
           "(f.dataFim IS NULL OR f.dataFim >= :agora)")
    List<Formulario> findFormulariosNoPeriodo(@Param("agora") LocalDateTime agora);

    /**
     * RF12: Busca formulários disponíveis para um usuário específico
     * Considera:
     * - Perfil do usuário
     * - Período ativo
     * - Formulários ativos
     */
    @Query("SELECT DISTINCT f FROM Formulario f " +
           "LEFT JOIN f.perfisDestino p " +
           "WHERE f.ativo = true AND " +
           "(p.id = :perfilId OR SIZE(f.perfisDestino) = 0) AND " +
           "(f.dataInicio IS NULL OR f.dataInicio <= :agora) AND " +
           "(f.dataFim IS NULL OR f.dataFim >= :agora)")
    List<Formulario> findFormulariosDisponiveisParaUsuario(
            @Param("perfilId") Integer perfilId,
            @Param("agora") LocalDateTime agora
    );

    /**
     * Busca formulários de um processo avaliativo que estão ativos
     */
    @Query("SELECT f FROM Formulario f WHERE f.processoAvaliativo.id = :processoId AND f.ativo = true")
    List<Formulario> findFormulariosAtivosPorProcesso(@Param("processoId") Integer processoId);

    /**
     * Busca formulários anônimos
     */
    List<Formulario> findByIsAnonimoTrue();

    /**
     * Busca formulários que permitem edição
     */
    List<Formulario> findByPermiteEdicaoTrue();

    /**
     * Busca formulários por título (busca parcial, case-insensitive)
     */
    @Query("SELECT f FROM Formulario f WHERE LOWER(f.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))")
    List<Formulario> findByTituloContaining(@Param("titulo") String titulo);

    /**
     * Busca todos os formulários ordenados por data de criação (mais recentes primeiro)
     */
    List<Formulario> findAllByOrderByCreatedAtDesc();
}
