package br.com.ecofood.model;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlimentoTest {

    @Test
    void deveAtualizarOCadastroSemTrocarOId() {
        Alimento alimento = new Alimento(
                "Leite", "r1", "c1", "Geladeira", new BigDecimal("2"), "L", LocalDate.of(2026, 11, 20));
        ReflectionTestUtils.setField(alimento, "id", "a1");
        Alimento novosDados = new Alimento(
                "Queijo", "r2", "c2", "Freezer", new BigDecimal("0.5"), "kg", LocalDate.of(2026, 12, 1));

        alimento.atualizarCadastro(novosDados);

        assertEquals("a1", alimento.getId());
        assertEquals("Queijo", alimento.getNome());
        assertEquals("r2", alimento.getResidenciaId());
        assertEquals("c2", alimento.getCategoriaId());
        assertEquals("Freezer", alimento.getLocal());
        assertEquals(new BigDecimal("0.5"), alimento.getQuantidade());
        assertEquals("kg", alimento.getUnidade());
        assertEquals(LocalDate.of(2026, 12, 1), alimento.getDataValidade());
    }
}
