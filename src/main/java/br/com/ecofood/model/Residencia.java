package br.com.ecofood.model;

import br.com.ecofood.exception.RegraDeNegocioException;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Document(collection = "residencias")
public class Residencia {

    @Id
    private final String id;
    private final String nome;
    private final String responsavel;
    private final Endereco endereco;
    private final List<LocalArmazenamento> locais;

    public Residencia(
            String id,
            String nome,
            String responsavel,
            Endereco endereco,
            List<LocalArmazenamento> locais
    ) {
        this.id = id;
        this.nome = nome;
        this.responsavel = responsavel;
        this.endereco = endereco;
        this.locais = new ArrayList<>();
        if (locais != null) {
            locais.forEach(this::incluirLocal);
        }
    }

    public boolean possuiLocal(String nomeDoLocal) {
        return locais.stream().anyMatch(local -> local.temNome(nomeDoLocal));
    }

    public void adicionarLocal(LocalArmazenamento local) {
        incluirLocal(local);
    }

    private void incluirLocal(LocalArmazenamento local) {
        if (possuiLocal(local.nome())) {
            throw new RegraDeNegocioException("A residência já possui o local: " + local.nome());
        }
        locais.add(local);
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public List<LocalArmazenamento> getLocais() {
        return Collections.unmodifiableList(locais);
    }
}
