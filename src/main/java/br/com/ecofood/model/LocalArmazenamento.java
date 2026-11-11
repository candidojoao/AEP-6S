package br.com.ecofood.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LocalArmazenamento(
        @NotBlank(message = "O nome do local é obrigatório")
        String nome,

        @NotNull(message = "O tipo do local é obrigatório")
        TipoLocal tipo
) {

    public LocalArmazenamento {
        nome = nome == null ? null : nome.trim();
    }

    public boolean temNome(String outroNome) {
        return nome != null && outroNome != null && nome.equalsIgnoreCase(outroNome.trim());
    }
}
