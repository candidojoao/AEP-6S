package br.com.ecofood.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "alimentos")
@CompoundIndex(name = "residencia_validade", def = "{'residenciaId': 1, 'dataValidade': 1}")
@JsonPropertyOrder({"id"})
public class Alimento {

    @Id
    private String id;
    private String nome;
    @Field(targetType = FieldType.OBJECT_ID)
    private String residenciaId;
    @Indexed
    @Field(targetType = FieldType.OBJECT_ID)
    private String categoriaId;
    private String local;
    private BigDecimal quantidade;
    private String unidade;
    private LocalDate dataValidade;

    public Alimento(
            String nome,
            String residenciaId,
            String categoriaId,
            String local,
            BigDecimal quantidade,
            String unidade,
            LocalDate dataValidade
    ) {
        this.nome = nome;
        this.residenciaId = residenciaId;
        this.categoriaId = categoriaId;
        this.local = local;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.dataValidade = dataValidade;
    }

    public void atualizarCadastro(Alimento dados) {
        nome = dados.nome;
        residenciaId = dados.residenciaId;
        categoriaId = dados.categoriaId;
        local = dados.local;
        quantidade = dados.quantidade;
        unidade = dados.unidade;
        dataValidade = dados.dataValidade;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getResidenciaId() {
        return residenciaId;
    }

    public String getCategoriaId() {
        return categoriaId;
    }

    public String getLocal() {
        return local;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }
}
