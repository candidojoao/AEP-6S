package br.com.ecofood.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Movimentacao(
        TipoMovimentacao tipo,
        BigDecimal quantidade,
        LocalDate data,
        String observacao
) {
}
