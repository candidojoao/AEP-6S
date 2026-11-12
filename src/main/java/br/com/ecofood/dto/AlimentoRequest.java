package br.com.ecofood.dto;

import br.com.ecofood.model.Alimento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AlimentoRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "Informe a residência")
        String residenciaId,

        @NotBlank(message = "Informe a categoria")
        String categoriaId,

        @NotBlank(message = "Informe o local de armazenamento")
        String local,

        @NotNull(message = "A quantidade é obrigatória")
        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "A quantidade deve ser maior que zero"
        )
        BigDecimal quantidade,

        @NotBlank(message = "A unidade é obrigatória")
        String unidade,

        @NotNull(message = "A data de validade é obrigatória")
        LocalDate dataValidade
) {

    public Alimento paraAlimento(String localDaResidencia) {
        return new Alimento(nome, residenciaId, categoriaId, localDaResidencia, quantidade, unidade, dataValidade);
    }
}
