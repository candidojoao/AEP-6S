package br.com.ecofood.integracao;

import br.com.ecofood.model.Alimento;
import br.com.ecofood.model.Categoria;
import br.com.ecofood.model.Endereco;
import br.com.ecofood.model.LocalArmazenamento;
import br.com.ecofood.model.Movimentacao;
import br.com.ecofood.model.Residencia;
import br.com.ecofood.model.TipoLocal;
import br.com.ecofood.model.TipoMovimentacao;
import br.com.ecofood.repository.AlimentoRepository;
import br.com.ecofood.repository.CategoriaRepository;
import br.com.ecofood.repository.ResidenciaRepository;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TesteDeIntegracaoMongo
class RepositoriosIntegracaoTest extends BancoEmbutido {

    @Autowired
    private ResidenciaRepository residencias;

    @Autowired
    private CategoriaRepository categorias;

    @Autowired
    private AlimentoRepository alimentos;

    private Residencia novaResidencia(String nome) {
        return residencias.save(new Residencia(
                null,
                nome,
                "Ana",
                new Endereco("Rua A", "100", "Centro", "Maringá", "PR", "87000-000"),
                List.of(
                        new LocalArmazenamento("Geladeira", TipoLocal.REFRIGERADO),
                        new LocalArmazenamento("Despensa", TipoLocal.AMBIENTE)
                )
        ));
    }

    private Categoria novaCategoria(String nome) {
        return categorias.save(new Categoria(null, nome, 3, List.of("Manter refrigerado")));
    }

    private Alimento novoAlimento(String nome, Residencia residencia, Categoria categoria, LocalDate validade) {
        return alimentos.save(new Alimento(
                nome, residencia.getId(), categoria.getId(), "Geladeira", new BigDecimal("2"), "L", validade));
    }

    private Document documentoBruto(String colecao, String id) {
        return mongo.getCollection(colecao).find(new Document("_id", new ObjectId(id))).first();
    }

    @Test
    void deveGravarAResidenciaComSubdocumentosESemCampoTecnico() {
        Residencia salva = novaResidencia("Casa da família Silva");

        Document documento = documentoBruto("residencias", salva.getId());

        assertInstanceOf(Document.class, documento.get("endereco"));
        assertEquals("Maringá", documento.get("endereco", Document.class).getString("cidade"));
        List<Document> locais = documento.getList("locais", Document.class);
        assertEquals(2, locais.size());
        assertEquals("REFRIGERADO", locais.get(0).getString("tipo"));
        assertFalse(documento.containsKey("_class"));
    }

    @Test
    void deveLerAResidenciaDeVoltaComOsLocais() {
        Residencia salva = novaResidencia("Casa da família Silva");

        Residencia lida = residencias.findById(salva.getId()).orElseThrow();

        assertEquals("PR", lida.getEndereco().estado());
        assertTrue(lida.possuiLocal("despensa"));
    }

    @Test
    void deveGravarReferenciasComoObjectIdEQuantidadesComoDecimal128() {
        Residencia casa = novaResidencia("Casa");
        Categoria laticinios = novaCategoria("Laticínios");
        Alimento leite = novoAlimento("Leite", casa, laticinios, LocalDate.of(2026, 11, 20));
        leite.registrarMovimentacao(new Movimentacao(
                TipoMovimentacao.CONSUMO, new BigDecimal("0.5"), LocalDate.of(2026, 11, 10), "Café"));
        alimentos.save(leite);

        Document documento = documentoBruto("alimentos", leite.getId());

        assertEquals(new ObjectId(casa.getId()), documento.get("residenciaId"));
        assertEquals(new ObjectId(laticinios.getId()), documento.get("categoriaId"));
        assertInstanceOf(Decimal128.class, documento.get("quantidade"));
        Document movimentacao = documento.getList("movimentacoes", Document.class).get(0);
        assertEquals("CONSUMO", movimentacao.getString("tipo"));
        assertInstanceOf(Decimal128.class, movimentacao.get("quantidade"));
    }

    @Test
    void deveLerOAlimentoDeVoltaComOHistorico() {
        Residencia casa = novaResidencia("Casa");
        Categoria laticinios = novaCategoria("Laticínios");
        Alimento leite = novoAlimento("Leite", casa, laticinios, LocalDate.of(2026, 11, 20));
        leite.registrarMovimentacao(new Movimentacao(
                TipoMovimentacao.DESCARTE, new BigDecimal("0.5"), LocalDate.of(2026, 11, 10), null));
        alimentos.save(leite);

        Alimento lido = alimentos.findById(leite.getId()).orElseThrow();

        assertEquals(casa.getId(), lido.getResidenciaId());
        assertEquals(0, new BigDecimal("1.5").compareTo(lido.getQuantidade()));
        assertEquals(LocalDate.of(2026, 11, 20), lido.getDataValidade());
        assertEquals(TipoMovimentacao.DESCARTE, lido.getMovimentacoes().get(0).tipo());
    }

    @Test
    void deveBuscarOsAlimentosDeUmaResidenciaEmOrdemDeValidade() {
        Residencia casa = novaResidencia("Casa");
        Residencia outraCasa = novaResidencia("Outra casa");
        Categoria laticinios = novaCategoria("Laticínios");
        novoAlimento("Queijo", casa, laticinios, LocalDate.of(2026, 12, 5));
        novoAlimento("Leite", casa, laticinios, LocalDate.of(2026, 11, 20));
        novoAlimento("Iogurte", outraCasa, laticinios, LocalDate.of(2026, 11, 1));

        List<Alimento> daCasa = alimentos.findByResidenciaIdOrderByDataValidadeAsc(casa.getId());

        assertEquals(List.of("Leite", "Queijo"), daCasa.stream().map(Alimento::getNome).toList());
    }

    @Test
    void deveSaberSeHaAlimentosLigadosAResidenciaEACategoria() {
        Residencia casa = novaResidencia("Casa");
        Residencia vazia = novaResidencia("Casa vazia");
        Categoria laticinios = novaCategoria("Laticínios");
        Categoria graos = novaCategoria("Grãos");
        novoAlimento("Leite", casa, laticinios, LocalDate.of(2026, 11, 20));

        assertTrue(alimentos.existsByResidenciaId(casa.getId()));
        assertFalse(alimentos.existsByResidenciaId(vazia.getId()));
        assertTrue(alimentos.existsByCategoriaId(laticinios.getId()));
        assertFalse(alimentos.existsByCategoriaId(graos.getId()));
    }

    @Test
    void deveBuscarCategoriaPeloNomeSemDiferenciarMaiusculas() {
        novaCategoria("Laticínios");

        assertTrue(categorias.findByNomeIgnoreCase("LATICÍNIOS").isPresent());
        assertTrue(categorias.findByNomeIgnoreCase("Grãos").isEmpty());
    }

    @Test
    void indiceUnicoDeveBarrarCategoriaComNomeRepetido() {
        novaCategoria("Grãos");

        assertThrows(DuplicateKeyException.class, () -> novaCategoria("Grãos"));
    }

    @Test
    void deveLerAlimentoGravadoNoFormatoDaPrimeiraEntrega() {
        mongo.getCollection("alimentos").insertOne(new Document("nome", "Arroz")
                .append("categoria", "Grãos")
                .append("quantidade", new Decimal128(new BigDecimal("5")))
                .append("unidade", "kg"));

        Alimento antigo = alimentos.findAll().get(0);

        assertEquals("Arroz", antigo.getNome());
        assertTrue(antigo.getMovimentacoes().isEmpty());
    }
}
