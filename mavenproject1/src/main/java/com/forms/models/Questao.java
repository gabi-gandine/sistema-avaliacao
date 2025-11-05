package com.forms.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

/**
 * Representa uma questão dentro de uma avaliação
 */
@Entity
@Table(name = "questao")
public class Questao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Texto da questão é obrigatório")
    @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
    private String texto;

    @NotNull(message = "Tipo da questão é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 50)
    private TipoQuestao tipo;

    @NotNull(message = "Ordem da questão é obrigatória")
    @Column(name = "ordem", nullable = false)
    private Integer ordem;

    /**
     * RF10: Indica se a questão é obrigatória ou opcional
     */
    @Column(name = "obrigatoria", nullable = false)
    private Boolean obrigatoria = true;

    @ManyToOne
    @JoinColumn(name = "avaliacaoId", referencedColumnName = "id", nullable = false)
    private Avaliacao avaliacao;

    @OneToMany(mappedBy = "questao", cascade = CascadeType.ALL)
    private Set<OpcaoResposta> opcoes;

    @OneToMany(mappedBy = "questao", cascade = CascadeType.ALL)
    private Set<Resposta> respostas;

    // Getters e Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public TipoQuestao getTipo() {
        return tipo;
    }

    public void setTipo(TipoQuestao tipo) {
        this.tipo = tipo;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public Boolean getObrigatoria() {
        return obrigatoria;
    }

    public void setObrigatoria(Boolean obrigatoria) {
        this.obrigatoria = obrigatoria;
    }

    public Avaliacao getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Avaliacao avaliacao) {
        this.avaliacao = avaliacao;
    }

    public Set<OpcaoResposta> getOpcoes() {
        return opcoes;
    }

    public void setOpcoes(Set<OpcaoResposta> opcoes) {
        this.opcoes = opcoes;
    }

    public Set<Resposta> getRespostas() {
        return respostas;
    }

    public void setRespostas(Set<Resposta> respostas) {
        this.respostas = respostas;
    }
}
