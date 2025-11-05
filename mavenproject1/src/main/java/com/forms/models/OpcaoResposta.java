package com.forms.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Representa uma opção de resposta para questões de múltipla escolha
 */
@Entity
@Table(name = "opcaoResposta")
public class OpcaoResposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Texto da opção é obrigatório")
    @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(name = "ordem", nullable = false)
    private Integer ordem;

    @ManyToOne
    @JoinColumn(name = "questaoId", referencedColumnName = "id", nullable = false)
    private Questao questao;

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

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public Questao getQuestao() {
        return questao;
    }

    public void setQuestao(Questao questao) {
        this.questao = questao;
    }
}
