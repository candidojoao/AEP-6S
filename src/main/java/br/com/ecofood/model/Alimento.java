package br.com.ecofood.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "alimentos")
public record Alimento(
        @Id String id,
        @NotBlank(message = "O nome é obrigatório") String nome,
        @NotBlank(message = "A categoria é obrigatória") String categoria,
        @NotNull(message = "A quantidade é obrigatória")
        @DecimalMin(value = "0.0", inclusive = false, message = "A quantidade deve ser maior que zero")
        BigDecimal quantidade,
        @NotBlank(message = "A unidade é obrigatória") String unidade,
        @NotNull(message = "A data de validade é obrigatória") LocalDate dataValidade
) {
}

