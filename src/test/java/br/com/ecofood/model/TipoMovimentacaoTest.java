package br.com.ecofood.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TipoMovimentacaoTest {

    private static final BigDecimal ESTOQUE = new BigDecimal("5");

    @Test
    void consumoDescarteEDoacaoDevemTirarDoEstoque() {
        assertEquals(new BigDecimal("3"), TipoMovimentacao.CONSUMO.aplicarEm(ESTOQUE, new BigDecimal("2")));
        assertEquals(new BigDecimal("4.5"), TipoMovimentacao.DESCARTE.aplicarEm(ESTOQUE, new BigDecimal("0.5")));
        assertEquals(BigDecimal.ZERO, TipoMovimentacao.DOACAO.aplicarEm(ESTOQUE, ESTOQUE));
        assertTrue(TipoMovimentacao.CONSUMO.isSaida());
        assertTrue(TipoMovimentacao.DESCARTE.isSaida());
        assertTrue(TipoMovimentacao.DOACAO.isSaida());
    }

    @Test
    void reposicaoDeveSomarAoEstoque() {
        assertEquals(new BigDecimal("7"), TipoMovimentacao.REPOSICAO.aplicarEm(ESTOQUE, new BigDecimal("2")));
        assertFalse(TipoMovimentacao.REPOSICAO.isSaida());
    }
}
