package br.com.ecofood.integracao;

import br.com.ecofood.dto.DesperdicioPorUnidade;
import br.com.ecofood.dto.ItemDescartado;
import br.com.ecofood.dto.RelatorioDesperdicio;
import br.com.ecofood.dto.TotalMovimentado;
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
import br.com.ecofood.repository.RelatorioDesperdicioRepository;
import br.com.ecofood.repository.ResidenciaRepository;
import br.com.ecofood.service.RelatorioDesperdicioService;
import br.com.ecofood.service.ResidenciaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static br.com.ecofood.model.TipoMovimentacao.CONSUMO;
import static br.com.ecofood.model.TipoMovimentacao.DESCARTE;
import static br.com.ecofood.model.TipoMovimentacao.DOACAO;
import static br.com.ecofood.model.TipoMovimentacao.REPOSICAO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TesteDeIntegracaoMongo
@Import({RelatorioDesperdicioService.class, ResidenciaService.class})
class RelatorioDesperdicioIntegracaoTest extends BancoEmbutido {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 10);

    @Autowired
    private ResidenciaRepository residencias;

    @Autowired
    private CategoriaRepository categorias;

    @Autowired
    private AlimentoRepository alimentos;

    @Autowired
    private RelatorioDesperdicioRepository relatorio;

    @Autowired
    private RelatorioDesperdicioService service;

    private Residencia casa;

    @BeforeEach
    void prepararMovimentacoes() {
        casa = novaResidencia("Casa da família Silva");
        Residencia vizinha = novaResidencia("Casa vizinha");
        Categoria categoria = categorias.save(new Categoria(null, "Diversos", 3, null));

        alimentoComMovimentacoes(casa, categoria, "Leite", "L", "3",
                mov(CONSUMO, "1"), mov(DESCARTE, "0.5"), mov(REPOSICAO, "1"));
        alimentoComMovimentacoes(casa, categoria, "Iogurte", "un", "6",
                mov(CONSUMO, "2"), mov(DOACAO, "1"), mov(DESCARTE, "3"));
        alimentoComMovimentacoes(casa, categoria, "Arroz", "kg", "5",
                mov(DESCARTE, "1"));
        alimentoComMovimentacoes(vizinha, categoria, "Pão", "un", "10",
                mov(DESCARTE, "10"));
    }

    private Residencia novaResidencia(String nome) {
        return residencias.save(new Residencia(
                null,
                nome,
                "Ana",
                new Endereco("Rua A", null, null, "Maringá", "PR", null),
                List.of(new LocalArmazenamento("Geladeira", TipoLocal.REFRIGERADO))
        ));
    }

    private Movimentacao mov(TipoMovimentacao tipo, String quantidade) {
        return new Movimentacao(tipo, new BigDecimal(quantidade), DIA, null);
    }

    private void alimentoComMovimentacoes(
            Residencia residencia,
            Categoria categoria,
            String nome,
            String unidade,
            String estoque,
            Movimentacao... movimentacoes
    ) {
        Alimento alimento = new Alimento(
                nome, residencia.getId(), categoria.getId(), "Geladeira", new BigDecimal(estoque), unidade, DIA);
        for (Movimentacao movimentacao : movimentacoes) {
            alimento.registrarMovimentacao(movimentacao);
        }
        alimentos.save(alimento);
    }

    private static void assertValor(String esperado, BigDecimal atual) {
        assertEquals(0, new BigDecimal(esperado).compareTo(atual), () -> "esperado " + esperado + " e veio " + atual);
    }

    @Test
    void deveSomarAsSaidasPorUnidadeETipoSoDaResidenciaInformada() {
        List<TotalMovimentado> totais = relatorio.totaisPorUnidadeETipo(casa.getId());

        assertEquals(6, totais.size());
        assertEquals(List.of("L", "L", "kg", "un", "un", "un"), totais.stream().map(TotalMovimentado::unidade).toList());
        assertEquals(List.of(CONSUMO, DESCARTE, DESCARTE, CONSUMO, DESCARTE, DOACAO),
                totais.stream().map(TotalMovimentado::tipo).toList());
        assertValor("1", totais.get(0).total());
        assertValor("0.5", totais.get(1).total());
        assertValor("1", totais.get(2).total());
        assertValor("3", totais.get(4).total());
        assertTrue(totais.stream().noneMatch(total -> total.tipo() == REPOSICAO));
    }

    @Test
    void deveListarOsMaisDescartadosDoMaiorParaOMenor() {
        List<ItemDescartado> itens = relatorio.maisDescartados(casa.getId(), 2);

        assertEquals(List.of("Iogurte", "Arroz"), itens.stream().map(ItemDescartado::nome).toList());
        assertEquals("un", itens.get(0).unidade());
        assertValor("3", itens.get(0).quantidade());
    }

    @Test
    void deveMontarORelatorioComOAproveitamentoDeCadaUnidade() {
        RelatorioDesperdicio resultado = service.gerar(casa.getId());

        assertEquals("Casa da família Silva", resultado.residencia());
        assertEquals(List.of("L", "kg", "un"),
                resultado.porUnidade().stream().map(DesperdicioPorUnidade::unidade).toList());

        DesperdicioPorUnidade iogurte = resultado.porUnidade().stream()
                .filter(unidade -> unidade.unidade().equals("un")).findFirst().orElseThrow();
        assertValor("2", iogurte.consumido());
        assertValor("1", iogurte.doado());
        assertValor("3", iogurte.descartado());
        assertValor("50.0", iogurte.aproveitamentoPercentual());

        DesperdicioPorUnidade leite = resultado.porUnidade().stream()
                .filter(unidade -> unidade.unidade().equals("L")).findFirst().orElseThrow();
        assertValor("66.7", leite.aproveitamentoPercentual());

        assertEquals(3, resultado.maisDescartados().size());
    }

    @Test
    void deveDevolverRelatorioVazioParaResidenciaSemMovimentacoes() {
        Residencia nova = novaResidencia("Casa nova");

        RelatorioDesperdicio resultado = service.gerar(nova.getId());

        assertTrue(resultado.porUnidade().isEmpty());
        assertTrue(resultado.maisDescartados().isEmpty());
    }
}
