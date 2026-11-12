package br.com.ecofood.model;

import br.com.ecofood.exception.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlimentoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 10);

    private Alimento leite;

    @BeforeEach
    void preparar() {
        leite = new Alimento("Leite", "r1", "c1", "Geladeira", new BigDecimal("2"), "L", LocalDate.of(2026, 11, 20));
        ReflectionTestUtils.setField(leite, "id", "a1");
    }

    private Movimentacao movimentacao(TipoMovimentacao tipo, String quantidade) {
        return new Movimentacao(tipo, new BigDecimal(quantidade), HOJE, null);
    }

    @Test
    void deveComecarSemMovimentacoes() {
        assertTrue(leite.getMovimentacoes().isEmpty());
    }

    @Test
    void consumoDeveBaixarOEstoqueEFicarNoHistorico() {
        leite.registrarMovimentacao(movimentacao(TipoMovimentacao.CONSUMO, "0.5"));

        assertEquals(new BigDecimal("1.5"), leite.getQuantidade());
        assertEquals(1, leite.getMovimentacoes().size());
        assertEquals(TipoMovimentacao.CONSUMO, leite.getMovimentacoes().get(0).tipo());
    }

    @Test
    void reposicaoDeveAumentarOEstoque() {
        leite.registrarMovimentacao(movimentacao(TipoMovimentacao.REPOSICAO, "1"));

        assertEquals(new BigDecimal("3"), leite.getQuantidade());
    }

    @Test
    void devePermitirZerarOEstoque() {
        leite.registrarMovimentacao(movimentacao(TipoMovimentacao.CONSUMO, "1.5"));
        leite.registrarMovimentacao(movimentacao(TipoMovimentacao.DESCARTE, "0.5"));

        assertEquals(0, leite.getQuantidade().signum());
        assertEquals(2, leite.getMovimentacoes().size());
    }

    @Test
    void naoDeveTirarMaisDoQueOEstoque() {
        Movimentacao demais = movimentacao(TipoMovimentacao.DOACAO, "3");

        RegraDeNegocioException erro =
                assertThrows(RegraDeNegocioException.class, () -> leite.registrarMovimentacao(demais));

        assertEquals("Quantidade maior que o estoque disponível: 2 L", erro.getMessage());
        assertEquals(new BigDecimal("2"), leite.getQuantidade());
        assertTrue(leite.getMovimentacoes().isEmpty());
    }

    @Test
    void deveContarOsDiasQueFaltamParaVencer() {
        assertEquals(10, leite.diasParaVencer(HOJE));
        assertEquals(0, leite.diasParaVencer(LocalDate.of(2026, 11, 20)));
        assertEquals(-2, leite.diasParaVencer(LocalDate.of(2026, 11, 22)));
    }

    @Test
    void deveSaberSeAindaTemEstoque() {
        assertTrue(leite.temEstoque());

        leite.registrarMovimentacao(movimentacao(TipoMovimentacao.CONSUMO, "2"));

        assertFalse(leite.temEstoque());
    }

    @Test
    void naoDevePermitirAlterarOHistoricoPorFora() {
        Movimentacao consumo = movimentacao(TipoMovimentacao.CONSUMO, "1");

        assertThrows(UnsupportedOperationException.class, () -> leite.getMovimentacoes().add(consumo));
    }

    @Test
    void deveAtualizarOCadastroSemTrocarOIdNemPerderOHistorico() {
        leite.registrarMovimentacao(movimentacao(TipoMovimentacao.CONSUMO, "1"));
        Alimento novosDados = new Alimento(
                "Queijo", "r2", "c2", "Freezer", new BigDecimal("0.5"), "kg", LocalDate.of(2026, 12, 1));

        leite.atualizarCadastro(novosDados);

        assertEquals("a1", leite.getId());
        assertEquals("Queijo", leite.getNome());
        assertEquals("r2", leite.getResidenciaId());
        assertEquals("c2", leite.getCategoriaId());
        assertEquals("Freezer", leite.getLocal());
        assertEquals(new BigDecimal("0.5"), leite.getQuantidade());
        assertEquals("kg", leite.getUnidade());
        assertEquals(LocalDate.of(2026, 12, 1), leite.getDataValidade());
        assertEquals(1, leite.getMovimentacoes().size());
    }
}
