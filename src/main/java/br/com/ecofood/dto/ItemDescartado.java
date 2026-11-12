package br.com.ecofood.dto;

import java.math.BigDecimal;

public record ItemDescartado(
        String nome,
        String unidade,
        BigDecimal quantidade
) {
}
