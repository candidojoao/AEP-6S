package br.com.ecofood.dto;

import br.com.ecofood.model.Categoria;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CategoriaRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotNull(message = "Informe com quantos dias de antecedência avisar o vencimento")
        @Min(value = 0, message = "Os dias de alerta não podem ser negativos")
        @Max(value = 365, message = "Os dias de alerta devem ser no máximo 365")
        Integer diasAlertaValidade,

        List<@NotBlank(message = "A dica não pode ficar em branco") String> dicasConservacao
) {

    public Categoria paraCategoria(String id) {
        return new Categoria(id, nome.trim(), diasAlertaValidade, dicasConservacao);
    }
}
