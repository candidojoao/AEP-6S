package br.com.ecofood.model;

import br.com.ecofood.exception.RegraDeNegocioException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private List<Movimentacao> movimentacoes = new ArrayList<>();

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

    public void registrarMovimentacao(Movimentacao movimentacao) {
        if (movimentacao.tipo().isSaida() && movimentacao.quantidade().compareTo(quantidade) > 0) {
            throw new RegraDeNegocioException("Quantidade maior que o estoque disponível: "
                    + quantidade.stripTrailingZeros().toPlainString() + " " + unidade);
        }
        quantidade = movimentacao.tipo().aplicarEm(quantidade, movimentacao.quantidade());
        movimentacoes.add(movimentacao);
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

    public List<Movimentacao> getMovimentacoes() {
        return Collections.unmodifiableList(movimentacoes);
    }
}
