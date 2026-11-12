package br.com.ecofood.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static br.com.ecofood.model.TipoMovimentacao.CONSUMO;
import static br.com.ecofood.model.TipoMovimentacao.DESCARTE;
import static br.com.ecofood.model.TipoMovimentacao.DOACAO;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DesperdicioPorUnidadeTest {

    @Test
    void deveCalcularOAproveitamentoComConsumoEDoacao() {
        DesperdicioPorUnidade resumo = DesperdicioPorUnidade.de("kg", List.of(
                new TotalMovimentado("kg", CONSUMO, new BigDecimal("6")),
                new TotalMovimentado("kg", DOACAO, new BigDecimal("2")),
                new TotalMovimentado("kg", DESCARTE, new BigDecimal("2"))
        ));

        assertEquals("kg", resumo.unidade());
        assertEquals(new BigDecimal("6"), resumo.consumido());
        assertEquals(new BigDecimal("2"), resumo.doado());
        assertEquals(new BigDecimal("2"), resumo.descartado());
        assertEquals(new BigDecimal("80.0"), resumo.aproveitamentoPercentual());
    }

    @Test
    void deveConsiderarZeroParaOTipoQueNaoApareceu() {
        DesperdicioPorUnidade soDescarte = DesperdicioPorUnidade.de("L", List.of(
                new TotalMovimentado("L", DESCARTE, new BigDecimal("1.5"))
        ));

        assertEquals(BigDecimal.ZERO, soDescarte.consumido());
        assertEquals(BigDecimal.ZERO, soDescarte.doado());
        assertEquals(new BigDecimal("0.0"), soDescarte.aproveitamentoPercentual());
    }

    @Test
    void deveArredondarParaUmaCasaDecimal() {
        DesperdicioPorUnidade resumo = DesperdicioPorUnidade.de("un", List.of(
                new TotalMovimentado("un", CONSUMO, new BigDecimal("2")),
                new TotalMovimentado("un", DESCARTE, new BigDecimal("1"))
        ));

        assertEquals(new BigDecimal("66.7"), resumo.aproveitamentoPercentual());
    }

    @Test
    void deveDevolverZeroQuandoNaoHouveSaidas() {
        assertEquals(BigDecimal.ZERO, DesperdicioPorUnidade.de("un", List.of()).aproveitamentoPercentual());
    }
}
