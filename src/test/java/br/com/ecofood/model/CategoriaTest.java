package br.com.ecofood.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoriaTest {

    @Test
    void deveGuardarOsDadosDaCategoria() {
        Categoria categoria = new Categoria("c1", "Laticínios", 3, List.of("Manter refrigerado"));

        assertEquals("c1", categoria.getId());
        assertEquals("Laticínios", categoria.getNome());
        assertEquals(3, categoria.getDiasAlertaValidade());
        assertEquals(List.of("Manter refrigerado"), categoria.getDicasConservacao());
    }

    @Test
    void deveAceitarCategoriaSemDicas() {
        Categoria categoria = new Categoria(null, "Grãos", 30, null);

        assertTrue(categoria.getDicasConservacao().isEmpty());
    }

    @Test
    void naoDeveSerAfetadaPorAlteracoesNaListaOriginal() {
        List<String> dicas = new ArrayList<>(List.of("Manter refrigerado"));
        Categoria categoria = new Categoria(null, "Laticínios", 3, dicas);

        dicas.add("Outra dica");

        assertEquals(1, categoria.getDicasConservacao().size());
        assertThrows(UnsupportedOperationException.class,
                () -> categoria.getDicasConservacao().add("Mais uma"));
    }
}
