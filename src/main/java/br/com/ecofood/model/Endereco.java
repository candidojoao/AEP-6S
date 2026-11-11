package br.com.ecofood.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record Endereco(
        @NotBlank(message = "A rua é obrigatória")
        String rua,

        String numero,

        String bairro,

        @NotBlank(message = "A cidade é obrigatória")
        String cidade,

        @NotBlank(message = "O estado é obrigatório")
        @Pattern(regexp = "[A-Za-z]{2}", message = "O estado deve ter 2 letras, como PR")
        String estado,

        String cep
) {

    public Endereco {
        estado = estado == null ? null : estado.trim().toUpperCase();
    }
}
