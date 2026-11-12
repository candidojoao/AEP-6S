package br.com.ecofood.service;

import br.com.ecofood.dto.CategoriaRequest;
import br.com.ecofood.exception.CategoriaNaoEncontradaException;
import br.com.ecofood.exception.RegraDeNegocioException;
import br.com.ecofood.model.Categoria;
import br.com.ecofood.repository.AlimentoRepository;
import br.com.ecofood.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository repository;

    @Mock
    private AlimentoRepository alimentos;

    private CategoriaService service;
    private CategoriaRequest request;

    @BeforeEach
    void preparar() {
        service = new CategoriaService(repository, alimentos);
        request = new CategoriaRequest("  Laticínios ", 3, List.of("Manter refrigerado"));
    }

    private void salvarDevolvendoOMesmoObjeto() {
        when(repository.save(any(Categoria.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    @Test
    void deveCadastrarCategoriaComNomeSemEspacos() {
        when(repository.findByNomeIgnoreCase("Laticínios")).thenReturn(Optional.empty());
        salvarDevolvendoOMesmoObjeto();

        Categoria cadastrada = service.cadastrar(request);

        assertNull(cadastrada.getId());
        assertEquals("Laticínios", cadastrada.getNome());
        assertEquals(3, cadastrada.getDiasAlertaValidade());
    }

    @Test
    void naoDeveCadastrarCategoriaComNomeRepetido() {
        when(repository.findByNomeIgnoreCase("Laticínios"))
                .thenReturn(Optional.of(new Categoria("c1", "Laticínios", 3, null)));

        RegraDeNegocioException erro =
                assertThrows(RegraDeNegocioException.class, () -> service.cadastrar(request));

        assertEquals("Já existe uma categoria com o nome: Laticínios", erro.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void deveAtualizarCategoriaMantendoOMesmoNome() {
        when(repository.existsById("c1")).thenReturn(true);
        when(repository.findByNomeIgnoreCase("Laticínios"))
                .thenReturn(Optional.of(new Categoria("c1", "Laticínios", 5, null)));
        salvarDevolvendoOMesmoObjeto();

        Categoria atualizada = service.atualizar("c1", request);

        assertEquals("c1", atualizada.getId());
        assertEquals(3, atualizada.getDiasAlertaValidade());
    }

    @Test
    void naoDeveAtualizarParaONomeDeOutraCategoria() {
        when(repository.existsById("c2")).thenReturn(true);
        when(repository.findByNomeIgnoreCase("Laticínios"))
                .thenReturn(Optional.of(new Categoria("c1", "Laticínios", 3, null)));

        assertThrows(RegraDeNegocioException.class, () -> service.atualizar("c2", request));
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveAtualizarCategoriaInexistente() {
        when(repository.existsById("999")).thenReturn(false);

        assertThrows(CategoriaNaoEncontradaException.class, () -> service.atualizar("999", request));
    }

    @Test
    void naoDeveExcluirCategoriaEmUso() {
        when(repository.existsById("c1")).thenReturn(true);
        when(alimentos.existsByCategoriaId("c1")).thenReturn(true);

        RegraDeNegocioException erro =
                assertThrows(RegraDeNegocioException.class, () -> service.excluir("c1"));

        assertEquals("A categoria está em uso por alimentos e não pode ser excluída", erro.getMessage());
        verify(repository, never()).deleteById(any());
    }

    @Test
    void deveExcluirCategoriaSemAlimentos() {
        when(repository.existsById("c1")).thenReturn(true);
        when(alimentos.existsByCategoriaId("c1")).thenReturn(false);

        service.excluir("c1");

        verify(repository).deleteById("c1");
    }

    @Test
    void deveInformarQuandoCategoriaNaoForEncontrada() {
        when(repository.findById("999")).thenReturn(Optional.empty());

        CategoriaNaoEncontradaException erro =
                assertThrows(CategoriaNaoEncontradaException.class, () -> service.buscarPorId("999"));

        assertEquals("Categoria não encontrada: 999", erro.getMessage());
    }
}
