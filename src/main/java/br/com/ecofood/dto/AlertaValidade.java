package br.com.ecofood.dto;

import br.com.ecofood.model.Alimento;
import br.com.ecofood.model.Categoria;
import br.com.ecofood.model.SituacaoValidade;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AlertaValidade(
        String alimentoId,
        String alimento,
        String categoria,
        String local,
        BigDecimal quantidade,
        String unidade,
        LocalDate dataValidade,
        long diasParaVencer,
        SituacaoValidade situacao
) {

    public static AlertaValidade de(Alimento alimento, Categoria categoria, LocalDate hoje) {
        long dias = alimento.diasParaVencer(hoje);
        return new AlertaValidade(
                alimento.getId(),
                alimento.getNome(),
                categoria.getNome(),
                alimento.getLocal(),
                alimento.getQuantidade(),
                alimento.getUnidade(),
                alimento.getDataValidade(),
                dias,
                SituacaoValidade.para(dias)
        );
    }
}
