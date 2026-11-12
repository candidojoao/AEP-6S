package br.com.ecofood.controller;

import br.com.ecofood.dto.AlertaValidade;
import br.com.ecofood.dto.DesperdicioPorUnidade;
import br.com.ecofood.dto.ItemDescartado;
import br.com.ecofood.dto.RelatorioDesperdicio;
import br.com.ecofood.exception.ResidenciaNaoEncontradaException;
import br.com.ecofood.model.SituacaoValidade;
import br.com.ecofood.service.AlertaValidadeService;
import br.com.ecofood.service.RelatorioDesperdicioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RelatorioController.class)
class RelatorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlertaValidadeService alertas;

    @MockitoBean
    private RelatorioDesperdicioService desperdicio;

    @Test
    void deveListarOsAlertasDeValidade() throws Exception {
        when(alertas.listar("r1")).thenReturn(List.of(new AlertaValidade(
                "a1", "Leite", "Laticínios", "Geladeira", new BigDecimal("2"), "L",
                LocalDate.of(2026, 11, 12), 2, SituacaoValidade.PERTO_DO_VENCIMENTO)));

        mockMvc.perform(get("/residencias/r1/alertas-validade"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].alimento").value("Leite"))
                .andExpect(jsonPath("$[0].diasParaVencer").value(2))
                .andExpect(jsonPath("$[0].situacao").value("PERTO_DO_VENCIMENTO"));
    }

    @Test
    void deveMostrarORelatorioDeDesperdicio() throws Exception {
        when(desperdicio.gerar("r1")).thenReturn(new RelatorioDesperdicio(
                "r1",
                "Casa da família Silva",
                List.of(new DesperdicioPorUnidade("kg", new BigDecimal("6"), new BigDecimal("2"),
                        new BigDecimal("2"), new BigDecimal("80.0"))),
                List.of(new ItemDescartado("Arroz", "kg", new BigDecimal("2")))
        ));

        mockMvc.perform(get("/residencias/r1/relatorio-desperdicio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residencia").value("Casa da família Silva"))
                .andExpect(jsonPath("$.porUnidade[0].aproveitamentoPercentual").value(80.0))
                .andExpect(jsonPath("$.maisDescartados[0].nome").value("Arroz"));
    }

    @Test
    void deveRetornar404ParaResidenciaInexistente() throws Exception {
        when(alertas.listar("999")).thenThrow(new ResidenciaNaoEncontradaException("999"));
        when(desperdicio.gerar("999")).thenThrow(new ResidenciaNaoEncontradaException("999"));

        mockMvc.perform(get("/residencias/999/alertas-validade"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/residencias/999/relatorio-desperdicio"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Residência não encontrada: 999"));
    }
}
