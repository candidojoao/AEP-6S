package br.com.ecofood.controller;

import br.com.ecofood.dto.AlertaValidade;
import br.com.ecofood.dto.RelatorioDesperdicio;
import br.com.ecofood.service.AlertaValidadeService;
import br.com.ecofood.service.RelatorioDesperdicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/residencias/{id}")
@Tag(name = "Relatórios", description = "Alertas de validade e desperdício de cada residência")
public class RelatorioController {

    private final AlertaValidadeService alertas;
    private final RelatorioDesperdicioService desperdicio;

    public RelatorioController(AlertaValidadeService alertas, RelatorioDesperdicioService desperdicio) {
        this.alertas = alertas;
        this.desperdicio = desperdicio;
    }

    @GetMapping("/alertas-validade")
    @Operation(summary = "Alimentos vencidos ou perto do vencimento, conforme os dias de alerta da categoria")
    public List<AlertaValidade> alertasDeValidade(@PathVariable String id) {
        return alertas.listar(id);
    }

    @GetMapping("/relatorio-desperdicio")
    @Operation(summary = "Total consumido, doado e descartado por unidade e o percentual de aproveitamento")
    public RelatorioDesperdicio relatorioDeDesperdicio(@PathVariable String id) {
        return desperdicio.gerar(id);
    }
}
