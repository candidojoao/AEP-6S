package br.com.ecofood.dto;

import br.com.ecofood.model.TipoMovimentacao;

import java.math.BigDecimal;

public record TotalMovimentado(
        String unidade,
        TipoMovimentacao tipo,
        BigDecimal total
) {
}
