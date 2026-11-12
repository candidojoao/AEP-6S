package br.com.ecofood.model;

import br.com.ecofood.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResidenciaTest {

    private static final Endereco ENDERECO =
            new Endereco("Rua A", "100", "Centro", "Maringá", "pr", "87000-000");

    private Residencia residenciaComGeladeira() {
        return new Residencia(
                "r1",
                "Casa da família Silva",
                "Ana",
                ENDERECO,
                List.of(new LocalArmazenamento("Geladeira", TipoLocal.REFRIGERADO))
        );
    }

    @Test
    void deveReconhecerLocalSemDiferenciarMaiusculas() {
        Residencia residencia = residenciaComGeladeira();

        assertTrue(residencia.possuiLocal("geladeira"));
        assertTrue(residencia.possuiLocal("  GELADEIRA "));
        assertFalse(residencia.possuiLocal("Freezer"));
        assertFalse(residencia.possuiLocal(null));
    }

    @Test
    void deveDevolverOLocalComONomeCadastrado() {
        Residencia residencia = residenciaComGeladeira();

        assertEquals("Geladeira", residencia.buscarLocal(" geladeira ").orElseThrow().nome());
        assertTrue(residencia.buscarLocal("Varanda").isEmpty());
    }

    @Test
    void naoDeveCriarResidenciaComLocaisRepetidos() {
        List<LocalArmazenamento> locaisRepetidos = List.of(
                new LocalArmazenamento("Geladeira", TipoLocal.REFRIGERADO),
                new LocalArmazenamento("geladeira", TipoLocal.REFRIGERADO)
        );

        RegraDeNegocioException erro = assertThrows(
                RegraDeNegocioException.class,
                () -> new Residencia(null, "Casa", "Ana", ENDERECO, locaisRepetidos)
        );

        assertEquals("A residência já possui o local: geladeira", erro.getMessage());
    }

    @Test
    void deveAdicionarNovoLocal() {
        Residencia residencia = residenciaComGeladeira();

        residencia.adicionarLocal(new LocalArmazenamento("Freezer", TipoLocal.CONGELADO));

        assertEquals(2, residencia.getLocais().size());
        assertTrue(residencia.possuiLocal("Freezer"));
    }

    @Test
    void naoDeveAdicionarLocalQueJaExiste() {
        Residencia residencia = residenciaComGeladeira();
        LocalArmazenamento repetido = new LocalArmazenamento("GELADEIRA", TipoLocal.REFRIGERADO);

        assertThrows(RegraDeNegocioException.class, () -> residencia.adicionarLocal(repetido));
        assertEquals(1, residencia.getLocais().size());
    }

    @Test
    void naoDevePermitirAlterarAListaDeLocaisPorFora() {
        Residencia residencia = residenciaComGeladeira();
        LocalArmazenamento despensa = new LocalArmazenamento("Despensa", TipoLocal.AMBIENTE);

        assertThrows(UnsupportedOperationException.class, () -> residencia.getLocais().add(despensa));
    }

    @Test
    void deveAceitarResidenciaSemListaDeLocais() {
        Residencia residencia = new Residencia(null, "Casa", "Ana", ENDERECO, null);

        assertTrue(residencia.getLocais().isEmpty());
        assertNull(residencia.getId());
    }

    @Test
    void deveExporOsDadosDaResidencia() {
        Residencia residencia = residenciaComGeladeira();

        assertEquals("r1", residencia.getId());
        assertEquals("Casa da família Silva", residencia.getNome());
        assertEquals("Ana", residencia.getResponsavel());
        assertEquals(ENDERECO, residencia.getEndereco());
    }

    @Test
    void enderecoDeveGuardarOEstadoEmMaiusculas() {
        assertEquals("PR", ENDERECO.estado());
        assertNull(new Endereco("Rua A", null, null, "Maringá", null, null).estado());
    }

    @Test
    void localDeveIgnorarEspacosNoNome() {
        LocalArmazenamento local = new LocalArmazenamento("  Despensa  ", TipoLocal.AMBIENTE);

        assertEquals("Despensa", local.nome());
        assertFalse(local.temNome(null));
        assertFalse(new LocalArmazenamento(null, TipoLocal.AMBIENTE).temNome("Despensa"));
    }
}
