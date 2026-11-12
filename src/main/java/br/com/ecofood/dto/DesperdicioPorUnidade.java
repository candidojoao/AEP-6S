package br.com.ecofood.dto;

import br.com.ecofood.model.TipoMovimentacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record DesperdicioPorUnidade(
        String unidade,
        BigDecimal consumido,
        BigDecimal doado,
        BigDecimal descartado,
        BigDecimal aproveitamentoPercentual
) {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    public static DesperdicioPorUnidade de(String unidade, List<TotalMovimentado> totais) {
        BigDecimal consumido = somar(totais, TipoMovimentacao.CONSUMO);
        BigDecimal doado = somar(totais, TipoMovimentacao.DOACAO);
        BigDecimal descartado = somar(totais, TipoMovimentacao.DESCARTE);
        return new DesperdicioPorUnidade(
                unidade, consumido, doado, descartado, aproveitamento(consumido.add(doado), descartado));
    }

    private static BigDecimal somar(List<TotalMovimentado> totais, TipoMovimentacao tipo) {
        return totais.stream()
                .filter(total -> total.tipo() == tipo)
                .map(TotalMovimentado::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal aproveitamento(BigDecimal aproveitado, BigDecimal descartado) {
        BigDecimal saidas = aproveitado.add(descartado);
        if (saidas.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return aproveitado.multiply(CEM).divide(saidas, 1, RoundingMode.HALF_UP);
    }
}
