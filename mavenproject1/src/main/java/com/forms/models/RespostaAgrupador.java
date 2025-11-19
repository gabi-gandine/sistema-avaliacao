package com.forms.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * RF14, RF19: Entidade CRUCIAL para implementar anonimato correto
 *
 * RespostaAgrupador funciona como uma camada de indireção entre
 * as Respostas e a identificação do usuário (SubmissaoControle).
 *
 * COMO FUNCIONA O ANONIMATO:
 *
 * Formulário IDENTIFICADO:
 * - Relatórios acessam: Resposta → RespostaAgrupador → SubmissaoControle → Usuario
 * - Mostra nome do aluno nas respostas
 *
 * Formulário ANÔNIMO:
 * - Relatórios acessam: Resposta → RespostaAgrupador (PARA AQUI!)
 * - NÃO segue o link para SubmissaoControle
 * - Portanto, não expõe o usuário
 * - Mas o sistema SEMPRE sabe quem respondeu via SubmissaoControle (RF03)
 *
 * Exemplo:
 * - Aluno "João" responde formulário anônimo
 * - Sistema cria: SubmissaoControle (vincula João ao formulário)
 * - Sistema cria: RespostaAgrupador (vincula ao SubmissaoControle)
 * - Sistema cria: Respostas (vinculadas ao RespostaAgrupador)
 * - Relatório mostra: Respostas do RespostaAgrupador #123 (sem expor que é João)
 * - Auditoria sabe: RespostaAgrupador #123 pertence a SubmissaoControle que é do João
 *
 * @author gabriela
 */
@Entity
@Table(name = "resposta_agrupador")
public class RespostaAgrupador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resposta_agrupador")
    private Integer id;

    /**
     * Link para SubmissaoControle (que tem o usuário)
     * Em formulários anônimos, relatórios NÃO seguem este link
     * Apenas auditoria e controle interno acessam
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_submissao_controle", referencedColumnName = "id_submissao_controle", nullable = false)
    private SubmissaoControle submissaoControle;

    /**
     * Turma do respondente (útil para relatórios agregados)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_turma", referencedColumnName = "id")
    private Turma turma;

    /**
     * Conjunto de respostas agrupadas
     * Representa todas as respostas de um usuário para um formulário
     */
    @OneToMany(mappedBy = "respostaAgrupador", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Resposta> respostas;

    /**
     * Timestamp de quando o agrupador foi criado (início da submissão)
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp de quando foi finalizado (submissão completa)
     */
    @Column(name = "finalizado_at")
    private LocalDateTime finalizadoAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters e Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SubmissaoControle getSubmissaoControle() {
        return submissaoControle;
    }

    public void setSubmissaoControle(SubmissaoControle submissaoControle) {
        this.submissaoControle = submissaoControle;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }

    public Set<Resposta> getRespostas() {
        return respostas;
    }

    public void setRespostas(Set<Resposta> respostas) {
        this.respostas = respostas;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getFinalizadoAt() {
        return finalizadoAt;
    }

    public void setFinalizadoAt(LocalDateTime finalizadoAt) {
        this.finalizadoAt = finalizadoAt;
    }

    // Métodos úteis

    /**
     * Adiciona uma resposta ao agrupador
     */
    public void adicionarResposta(Resposta resposta) {
        if (this.respostas == null) {
            this.respostas = new java.util.HashSet<>();
        }
        this.respostas.add(resposta);
        resposta.setRespostaAgrupador(this);
    }

    /**
     * Finaliza o agrupador (marca timestamp de finalização)
     */
    public void finalizar() {
        this.finalizadoAt = LocalDateTime.now();
    }

    /**
     * Verifica se o agrupador foi finalizado
     */
    public boolean isFinalizado() {
        return finalizadoAt != null;
    }

    /**
     * APENAS para auditoria e controle: obtém o usuário que respondeu
     * NUNCA usar em relatórios de formulários anônimos!
     */
    public Usuario getUsuarioParaAuditoria() {
        if (submissaoControle != null) {
            return submissaoControle.getUsuario();
        }
        return null;
    }

    /**
     * Obtém o formulário através do SubmissaoControle
     */
    public Formulario getFormulario() {
        if (submissaoControle != null) {
            return submissaoControle.getFormulario();
        }
        return null;
    }

    /**
     * Alias para getFinalizadoAt() - compatibilidade com controllers
     */
    public LocalDateTime getDataFinalizacao() {
        return finalizadoAt;
    }

    /**
     * Verifica se a resposta pode ser editada
     * Depende das configurações do formulário
     */
    public boolean podeEditar() {
        Formulario formulario = getFormulario();
        if (formulario == null) {
            return false;
        }

        // Não pode editar se for anônimo
        if (formulario.getIsAnonimo()) {
            return false;
        }

        // Não pode editar se o formulário não permitir edição
        if (!formulario.getPermiteEdicao()) {
            return false;
        }

        // Pode editar apenas se foi finalizado
        if (!isFinalizado()) {
            return false;
        }

        return true;
    }

    /**
     * Alias para isFinalizado() - compatibilidade com controllers
     */
    public Boolean getFinalizada() {
        return isFinalizado();
    }
}
