package br.com.ecofood.dto;

import br.com.ecofood.model.Movimentacao;
import br.com.ecofood.model.TipoMovimentacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimentacaoRequest(
        @NotNull(message = "Informe o tipo da movimentação")
        TipoMovimentacao tipo,

        @NotNull(message = "A quantidade é obrigatória")
        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "A quantidade deve ser maior que zero"
        )
        BigDecimal quantidade,

        @PastOrPresent(message = "A data não pode estar no futuro")
        LocalDate data,

        @Size(max = 200, message = "A observação deve ter no máximo 200 caracteres")
        String observacao
) {

    public Movimentacao paraMovimentacao(LocalDate hoje) {
        return new Movimentacao(tipo, quantidade, data == null ? hoje : data, observacao);
    }
}
