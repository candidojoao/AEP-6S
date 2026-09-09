package br.com.ecofood.service;

import br.com.ecofood.exception.AlimentoNaoEncontradoException;
import br.com.ecofood.model.Alimento;
import br.com.ecofood.repository.AlimentoRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlimentoServiceTest {

    @Mock
    private AlimentoRepository repository;

    private AlimentoService service;
    private Alimento alimento;

    @BeforeEach
    void preparar() {
        service = new AlimentoService(repository);
        alimento = new Alimento(
                "001",
                "Arroz",
                "Grãos",
                new BigDecimal("5"),
                "kg",
                LocalDate.of(2026, 10, 15)
        );
    }

    @Test
    void deveCadastrarAlimentoSemAceitarIdDoCorpo() {
        when(repository.save(org.mockito.ArgumentMatchers.any(Alimento.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Alimento cadastrado = service.cadastrar(alimento);

        assertNull(cadastrado.id());
        assertEquals("Arroz", cadastrado.nome());
        verify(repository).save(cadastrado);
    }

    @Test
    void deveListarAlimentos() {
        when(repository.findAll()).thenReturn(List.of(alimento));

        List<Alimento> alimentos = service.listar();

        assertEquals(List.of(alimento), alimentos);
    }

    @Test
    void deveBuscarAlimentoPorId() {
        when(repository.findById("001")).thenReturn(Optional.of(alimento));

        Alimento encontrado = service.buscarPorId("001");

        assertEquals(alimento, encontrado);
    }

    @Test
    void deveInformarQuandoAlimentoNaoForEncontrado() {
        when(repository.findById("999")).thenReturn(Optional.empty());

        AlimentoNaoEncontradoException erro = assertThrows(
                AlimentoNaoEncontradoException.class,
                () -> service.buscarPorId("999")
        );

        assertEquals("Alimento não encontrado: 999", erro.getMessage());
    }

    @Test
    void deveAtualizarAlimentoMantendoIdDaUrl() {
        Alimento novosDados = new Alimento(
                "id-ignorado",
                "Feijão",
                "Grãos",
                new BigDecimal("2.5"),
                "kg",
                LocalDate.of(2026, 12, 1)
        );
        when(repository.existsById("001")).thenReturn(true);
        when(repository.save(org.mockito.ArgumentMatchers.any(Alimento.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Alimento atualizado = service.atualizar("001", novosDados);

        assertEquals("001", atualizado.id());
        assertEquals("Feijão", atualizado.nome());
        verify(repository).save(atualizado);
    }

    @Test
    void naoDeveAtualizarAlimentoInexistente() {
        when(repository.existsById("999")).thenReturn(false);

        assertThrows(
                AlimentoNaoEncontradoException.class,
                () -> service.atualizar("999", alimento)
        );

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deveExcluirAlimentoExistente() {
        when(repository.existsById("001")).thenReturn(true);

        service.excluir("001");

        verify(repository).deleteById("001");
    }

    @Test
    void naoDeveExcluirAlimentoInexistente() {
        when(repository.existsById("999")).thenReturn(false);

        assertThrows(AlimentoNaoEncontradoException.class, () -> service.excluir("999"));

        verify(repository, never()).deleteById("999");
    }
}

