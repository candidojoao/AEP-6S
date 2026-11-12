package br.com.ecofood.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "categorias")
public class Categoria {

    @Id
    private final String id;

    @Indexed(unique = true)
    private final String nome;

    private final int diasAlertaValidade;
    private final List<String> dicasConservacao;

    public Categoria(String id, String nome, int diasAlertaValidade, List<String> dicasConservacao) {
        this.id = id;
        this.nome = nome;
        this.diasAlertaValidade = diasAlertaValidade;
        this.dicasConservacao = dicasConservacao == null ? List.of() : List.copyOf(dicasConservacao);
    }

    public boolean deveAlertar(long diasParaVencer) {
        return diasParaVencer <= diasAlertaValidade;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getDiasAlertaValidade() {
        return diasAlertaValidade;
    }

    public List<String> getDicasConservacao() {
        return dicasConservacao;
    }
}
