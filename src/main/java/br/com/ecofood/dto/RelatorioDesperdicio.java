package br.com.ecofood.dto;

import java.util.List;

public record RelatorioDesperdicio(
        String residenciaId,
        String residencia,
        List<DesperdicioPorUnidade> porUnidade,
        List<ItemDescartado> maisDescartados
) {
}
