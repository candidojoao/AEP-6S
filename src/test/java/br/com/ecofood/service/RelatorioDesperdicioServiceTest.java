package br.com.ecofood.service;

import br.com.ecofood.dto.DesperdicioPorUnidade;
import br.com.ecofood.dto.ItemDescartado;
import br.com.ecofood.dto.RelatorioDesperdicio;
import br.com.ecofood.dto.TotalMovimentado;
import br.com.ecofood.exception.ResidenciaNaoEncontradaException;
import br.com.ecofood.model.Endereco;
import br.com.ecofood.model.Residencia;
import br.com.ecofood.repository.RelatorioDesperdicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static br.com.ecofood.model.TipoMovimentacao.CONSUMO;
import static br.com.ecofood.model.TipoMovimentacao.DESCARTE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioDesperdicioServiceTest {

    @Mock
    private ResidenciaService residencias;

    @Mock
    private RelatorioDesperdicioRepository relatorio;

    private RelatorioDesperdicioService service;

    @BeforeEach
    void preparar() {
        service = new RelatorioDesperdicioService(residencias, relatorio);
    }

    @Test
    void deveJuntarOsTotaisDeCadaUnidade() {
        when(residencias.buscarPorId("r1")).thenReturn(new Residencia(
                "r1", "Casa da família Silva", "Ana", new Endereco("Rua A", null, null, "Maringá", "PR", null), null));
        when(relatorio.totaisPorUnidadeETipo("r1")).thenReturn(List.of(
                new TotalMovimentado("un", CONSUMO, new BigDecimal("3")),
                new TotalMovimentado("kg", DESCARTE, new BigDecimal("1")),
                new TotalMovimentado("un", DESCARTE, new BigDecimal("1"))
        ));
        List<ItemDescartado> descartados = List.of(new ItemDescartado("Arroz", "kg", BigDecimal.ONE));
        when(relatorio.maisDescartados("r1", 5)).thenReturn(descartados);

        RelatorioDesperdicio resultado = service.gerar("r1");

        assertEquals("r1", resultado.residenciaId());
        assertEquals("Casa da família Silva", resultado.residencia());
        assertEquals(List.of("kg", "un"), resultado.porUnidade().stream().map(DesperdicioPorUnidade::unidade).toList());
        assertEquals(new BigDecimal("75.0"), resultado.porUnidade().get(1).aproveitamentoPercentual());
        assertEquals(descartados, resultado.maisDescartados());
    }

    @Test
    void naoDeveConsultarOBancoParaResidenciaInexistente() {
        when(residencias.buscarPorId("999")).thenThrow(new ResidenciaNaoEncontradaException("999"));

        assertThrows(ResidenciaNaoEncontradaException.class, () -> service.gerar("999"));
        verify(relatorio, never()).totaisPorUnidadeETipo(anyString());
        verify(relatorio, never()).maisDescartados(anyString(), anyInt());
    }
}
