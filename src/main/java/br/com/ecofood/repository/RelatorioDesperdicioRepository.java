package br.com.ecofood.repository;

import br.com.ecofood.dto.ItemDescartado;
import br.com.ecofood.dto.TotalMovimentado;
import br.com.ecofood.model.TipoMovimentacao;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
public class RelatorioDesperdicioRepository {

    private static final String COLECAO = "alimentos";
    private static final List<String> SAIDAS = Arrays.stream(TipoMovimentacao.values())
            .filter(TipoMovimentacao::isSaida)
            .map(Enum::name)
            .toList();

    private final MongoTemplate mongo;

    public RelatorioDesperdicioRepository(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public List<TotalMovimentado> totaisPorUnidadeETipo(String residenciaId) {
        Aggregation pipeline = newAggregation(
                match(where("residenciaId").is(new ObjectId(residenciaId))),
                unwind("movimentacoes"),
                match(where("movimentacoes.tipo").in(SAIDAS)),
                group("unidade", "movimentacoes.tipo").sum("movimentacoes.quantidade").as("total"),
                project("unidade", "tipo", "total"),
                sort(Sort.Direction.ASC, "unidade", "tipo")
        );
        return mongo.aggregate(pipeline, COLECAO, TotalMovimentado.class).getMappedResults();
    }

    public List<ItemDescartado> maisDescartados(String residenciaId, int quantidadeDeItens) {
        Aggregation pipeline = newAggregation(
                match(where("residenciaId").is(new ObjectId(residenciaId))),
                unwind("movimentacoes"),
                match(where("movimentacoes.tipo").is(TipoMovimentacao.DESCARTE.name())),
                group("nome", "unidade").sum("movimentacoes.quantidade").as("quantidade"),
                sort(Sort.Direction.DESC, "quantidade"),
                limit(quantidadeDeItens),
                project("nome", "unidade", "quantidade")
        );
        return mongo.aggregate(pipeline, COLECAO, ItemDescartado.class).getMappedResults();
    }
}
