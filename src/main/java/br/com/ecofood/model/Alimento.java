package br.com.ecofood.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "alimentos")
public record Alimento(
        @Id String id,
        String nome,
        String categoria,
        BigDecimal quantidade,
        String unidade,
        LocalDate dataValidade
) {
}
