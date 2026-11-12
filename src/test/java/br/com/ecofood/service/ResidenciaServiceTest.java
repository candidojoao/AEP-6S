package br.com.ecofood.service;

import br.com.ecofood.dto.ResidenciaRequest;
import br.com.ecofood.exception.RegraDeNegocioException;
import br.com.ecofood.exception.ResidenciaNaoEncontradaException;
import br.com.ecofood.model.Alimento;
import br.com.ecofood.model.Endereco;
import br.com.ecofood.model.LocalArmazenamento;
import br.com.ecofood.model.Residencia;
import br.com.ecofood.model.TipoLocal;
import br.com.ecofood.repository.AlimentoRepository;
import br.com.ecofood.repository.ResidenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResidenciaServiceTest {

    @Mock
    private ResidenciaRepository repository;

    @Mock
    private AlimentoRepository alimentos;

    private ResidenciaService service;
    private ResidenciaRequest request;

    @BeforeEach
    void preparar() {
        service = new ResidenciaService(repository, alimentos);
        request = new ResidenciaRequest(
                "Casa da família Silva",
                "Ana",
                new Endereco("Rua A", "100", "Centro", "Maringá", "PR", "87000-000"),
                List.of(new LocalArmazenamento("Geladeira", TipoLocal.REFRIGERADO))
        );
    }

    private void salvarDevolvendoOMesmoObjeto() {
        when(repository.save(any(Residencia.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    @Test
    void deveCadastrarResidenciaDeixandoOIdParaOBanco() {
        salvarDevolvendoOMesmoObjeto();

        Residencia cadastrada = service.cadastrar(request);

        assertNull(cadastrada.getId());
        assertEquals("Casa da família Silva", cadastrada.getNome());
        assertEquals("Maringá", cadastrada.getEndereco().cidade());
        assertTrue(cadastrada.possuiLocal("Geladeira"));
    }

    @Test
    void deveAtualizarResidenciaUsandoOIdDaUrl() {
        when(repository.existsById("r1")).thenReturn(true);
        salvarDevolvendoOMesmoObjeto();

        Residencia atualizada = service.atualizar("r1", request);

        assertEquals("r1", atualizada.getId());
    }

    @Test
    void naoDeveAtualizarResidenciaInexistente() {
        when(repository.existsById("999")).thenReturn(false);

        assertThrows(ResidenciaNaoEncontradaException.class, () -> service.atualizar("999", request));
        verify(repository, never()).save(any());
    }

    @Test
    void deveInformarQuandoResidenciaNaoForEncontrada() {
        when(repository.findById("999")).thenReturn(Optional.empty());

        ResidenciaNaoEncontradaException erro = assertThrows(
                ResidenciaNaoEncontradaException.class,
                () -> service.buscarPorId("999")
        );

        assertEquals("Residência não encontrada: 999", erro.getMessage());
    }

    @Test
    void deveAdicionarLocalNaResidenciaExistente() {
        when(repository.findById("r1")).thenReturn(Optional.of(request.paraResidencia("r1")));
        salvarDevolvendoOMesmoObjeto();

        Residencia atualizada = service.adicionarLocal(
                "r1", new LocalArmazenamento("Freezer", TipoLocal.CONGELADO));

        assertEquals(2, atualizada.getLocais().size());
        assertTrue(atualizada.possuiLocal("Freezer"));
    }

    @Test
    void naoDeveAdicionarLocalRepetido() {
        when(repository.findById("r1")).thenReturn(Optional.of(request.paraResidencia("r1")));
        LocalArmazenamento repetido = new LocalArmazenamento("geladeira", TipoLocal.REFRIGERADO);

        assertThrows(RegraDeNegocioException.class, () -> service.adicionarLocal("r1", repetido));
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveAdicionarLocalEmResidenciaInexistente() {
        when(repository.findById("999")).thenReturn(Optional.empty());
        LocalArmazenamento freezer = new LocalArmazenamento("Freezer", TipoLocal.CONGELADO);

        assertThrows(ResidenciaNaoEncontradaException.class, () -> service.adicionarLocal("999", freezer));
    }

    @Test
    void naoDeveRemoverLocalQueTemAlimentosGuardados() {
        when(repository.existsById("r1")).thenReturn(true);
        when(alimentos.findByResidenciaIdOrderByDataValidadeAsc("r1")).thenReturn(List.of(
                new Alimento("Pão", "r1", "c1", "Despensa", BigDecimal.ONE, "un", LocalDate.of(2026, 11, 15))
        ));

        RegraDeNegocioException erro =
                assertThrows(RegraDeNegocioException.class, () -> service.atualizar("r1", request));

        assertEquals("O local Despensa tem alimentos guardados e não pode ser removido", erro.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void deveAtualizarQuandoOsLocaisEmUsoContinuam() {
        when(repository.existsById("r1")).thenReturn(true);
        when(alimentos.findByResidenciaIdOrderByDataValidadeAsc("r1")).thenReturn(List.of(
                new Alimento("Leite", "r1", "c1", "Geladeira", BigDecimal.ONE, "L", LocalDate.of(2026, 11, 15))
        ));
        salvarDevolvendoOMesmoObjeto();

        Residencia atualizada = service.atualizar("r1", request);

        assertTrue(atualizada.possuiLocal("Geladeira"));
    }

    @Test
    void naoDeveExcluirResidenciaComAlimentos() {
        when(repository.existsById("r1")).thenReturn(true);
        when(alimentos.existsByResidenciaId("r1")).thenReturn(true);

        RegraDeNegocioException erro =
                assertThrows(RegraDeNegocioException.class, () -> service.excluir("r1"));

        assertEquals("A residência tem alimentos cadastrados e não pode ser excluída", erro.getMessage());
        verify(repository, never()).deleteById(any());
    }

    @Test
    void deveListarEExcluirResidencias() {
        Residencia residencia = request.paraResidencia("r1");
        when(repository.findAll()).thenReturn(List.of(residencia));
        when(repository.existsById("r1")).thenReturn(true);

        assertEquals(List.of(residencia), service.listar());

        service.excluir("r1");
        verify(repository).deleteById("r1");
    }
}
