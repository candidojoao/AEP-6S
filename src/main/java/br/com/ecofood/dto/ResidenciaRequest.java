package br.com.ecofood.dto;

import br.com.ecofood.model.Endereco;
import br.com.ecofood.model.LocalArmazenamento;
import br.com.ecofood.model.Residencia;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ResidenciaRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O responsável é obrigatório")
        String responsavel,

        @NotNull(message = "O endereço é obrigatório")
        @Valid
        Endereco endereco,

        @NotEmpty(message = "Informe pelo menos um local de armazenamento")
        List<@NotNull @Valid LocalArmazenamento> locais
) {

    public Residencia paraResidencia(String id) {
        return new Residencia(id, nome, responsavel, endereco, locais);
    }
}
