package br.com.ecofood.service;

import br.com.ecofood.dto.AlertaValidade;
import br.com.ecofood.exception.ResidenciaNaoEncontradaException;
import br.com.ecofood.model.Alimento;
import br.com.ecofood.model.Categoria;
import br.com.ecofood.model.Movimentacao;
import br.com.ecofood.model.SituacaoValidade;
import br.com.ecofood.model.TipoMovimentacao;
import br.com.ecofood.repository.AlimentoRepository;
import br.com.ecofood.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaValidadeServiceTest {

    private static final Clock RELOGIO = Clock.fixed(
            Instant.parse("2026-11-10T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 10);

    @Mock
    private ResidenciaService residencias;

    @Mock
    private AlimentoRepository alimentos;

    @Mock
    private CategoriaRepository categorias;

    private AlertaValidadeService service;

    private final Categoria laticinios = new Categoria("c1", "Laticínios", 3, null);
    private final Categoria graos = new Categoria("c2", "Grãos", 30, null);

    @BeforeEach
    void preparar() {
        service = new AlertaValidadeService(residencias, alimentos, categorias, RELOGIO);
    }

    private Alimento alimento(String id, String nome, Categoria categoria, int diasParaVencer) {
        Alimento alimento = new Alimento(
                nome, "r1", categoria.getId(), "Despensa", BigDecimal.ONE, "un", HOJE.plusDays(diasParaVencer));
        ReflectionTestUtils.setField(alimento, "id", id);
        return alimento;
    }

    private void naResidencia(Alimento... lista) {
        when(alimentos.findByResidenciaIdOrderByDataValidadeAsc("r1")).thenReturn(List.of(lista));
        when(categorias.findAllById(anyList())).thenReturn(List.of(laticinios, graos));
    }

    @Test
    void deveAlertarConformeOsDiasDeCadaCategoria() {
        naResidencia(
                alimento("a1", "Leite", laticinios, 2),
                alimento("a2", "Queijo", laticinios, 5),
                alimento("a3", "Arroz", graos, 20),
                alimento("a4", "Feijão", graos, 40)
        );

        List<AlertaValidade> alertas = service.listar("r1");

        assertEquals(List.of("Leite", "Arroz"), alertas.stream().map(AlertaValidade::alimento).toList());
    }

    @Test
    void deveInformarASituacaoDeCadaAlerta() {
        naResidencia(
                alimento("a1", "Iogurte", laticinios, -1),
                alimento("a2", "Leite", laticinios, 0),
                alimento("a3", "Requeijão", laticinios, 3)
        );

        List<AlertaValidade> alertas = service.listar("r1");

        assertEquals(List.of(SituacaoValidade.VENCIDO, SituacaoValidade.VENCE_HOJE, SituacaoValidade.PERTO_DO_VENCIMENTO),
                alertas.stream().map(AlertaValidade::situacao).toList());
        AlertaValidade vencido = alertas.get(0);
        assertEquals("a1", vencido.alimentoId());
        assertEquals("Laticínios", vencido.categoria());
        assertEquals("Despensa", vencido.local());
        assertEquals(-1, vencido.diasParaVencer());
        assertEquals(HOJE.minusDays(1), vencido.dataValidade());
    }

    @Test
    void naoDeveAlertarAlimentoQueJaAcabou() {
        Alimento consumido = alimento("a1", "Leite", laticinios, 1);
        consumido.registrarMovimentacao(new Movimentacao(TipoMovimentacao.CONSUMO, BigDecimal.ONE, HOJE, null));
        when(alimentos.findByResidenciaIdOrderByDataValidadeAsc("r1")).thenReturn(List.of(consumido));

        assertTrue(service.listar("r1").isEmpty());
    }

    @Test
    void naoDeveListarAlertasDeResidenciaInexistente() {
        when(residencias.buscarPorId("999")).thenThrow(new ResidenciaNaoEncontradaException("999"));

        assertThrows(ResidenciaNaoEncontradaException.class, () -> service.listar("999"));
    }
}
