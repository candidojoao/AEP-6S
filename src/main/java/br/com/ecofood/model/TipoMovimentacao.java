package br.com.ecofood.model;

import java.math.BigDecimal;

public enum TipoMovimentacao {
    CONSUMO(true),
    DESCARTE(true),
    DOACAO(true),
    REPOSICAO(false);

    private final boolean saida;

    TipoMovimentacao(boolean saida) {
        this.saida = saida;
    }

    public boolean isSaida() {
        return saida;
    }

    public BigDecimal aplicarEm(BigDecimal estoque, BigDecimal quantidade) {
        return saida ? estoque.subtract(quantidade) : estoque.add(quantidade);
    }
}
