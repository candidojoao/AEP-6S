package br.com.ecofood.service;

import br.com.ecofood.dto.AlimentoRequest;
import br.com.ecofood.dto.MovimentacaoRequest;
import br.com.ecofood.exception.AlimentoNaoEncontradoException;
import br.com.ecofood.exception.CategoriaNaoEncontradaException;
import br.com.ecofood.exception.RegraDeNegocioException;
import br.com.ecofood.exception.ResidenciaNaoEncontradaException;
import br.com.ecofood.model.Alimento;
import br.com.ecofood.model.Categoria;
import br.com.ecofood.model.Endereco;
import br.com.ecofood.model.LocalArmazenamento;
import br.com.ecofood.model.Movimentacao;
import br.com.ecofood.model.Residencia;
import br.com.ecofood.model.TipoLocal;
import br.com.ecofood.model.TipoMovimentacao;
import br.com.ecofood.repository.AlimentoRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlimentoServiceTest {

    private static final Clock RELOGIO = Clock.fixed(
            Instant.parse("2026-11-10T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    @Mock
    private AlimentoRepository repository;

    @Mock
    private ResidenciaService residencias;

    @Mock
    private CategoriaService categorias;

    private AlimentoService service;

    @BeforeEach
    void preparar() {
        service = new AlimentoService(repository, residencias, categorias, RELOGIO);
    }

    private AlimentoRequest request(String local) {
        return new AlimentoRequest(
                "Leite",
                "r1",
                "c1",
                local,
                new BigDecimal("2"),
                "L",
                LocalDate.of(2026, 11, 20)
        );
    }

    private Residencia casaComGeladeira() {
        return new Residencia(
                "r1",
                "Casa da família Silva",
                "Ana",
                new Endereco("Rua A", "100", "Centro", "Maringá", "PR", null),
                List.of(new LocalArmazenamento("Geladeira", TipoLocal.REFRIGERADO))
        );
    }

    private Alimento alimentoSalvo(String id) {
        Alimento alimento = request("Geladeira").paraAlimento("Geladeira");
        ReflectionTestUtils.setField(alimento, "id", id);
        return alimento;
    }

    private void referenciasExistem() {
        when(categorias.buscarPorId("c1")).thenReturn(new Categoria("c1", "Laticínios", 3, null));
        when(residencias.buscarPorId("r1")).thenReturn(casaComGeladeira());
    }

    private void salvarDevolvendoOMesmoObjeto() {
        when(repository.save(any(Alimento.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    @Test
    void deveCadastrarAlimentoLigadoAResidenciaCategoriaELocal() {
        referenciasExistem();
        salvarDevolvendoOMesmoObjeto();

        Alimento cadastrado = service.cadastrar(request("Geladeira"));

        assertNull(cadastrado.getId());
        assertEquals("r1", cadastrado.getResidenciaId());
        assertEquals("c1", cadastrado.getCategoriaId());
        assertEquals("Geladeira", cadastrado.getLocal());
    }

    @Test
    void deveGravarONomeDoLocalComoEstaNaResidencia() {
        referenciasExistem();
        salvarDevolvendoOMesmoObjeto();

        Alimento cadastrado = service.cadastrar(request("  geladeira "));

        assertEquals("Geladeira", cadastrado.getLocal());
    }

    @Test
    void naoDeveCadastrarComLocalQueAResidenciaNaoTem() {
        referenciasExistem();

        RegraDeNegocioException erro = assertThrows(
                RegraDeNegocioException.class,
                () -> service.cadastrar(request("Varanda"))
        );

        assertEquals("A residência Casa da família Silva não possui o local: Varanda", erro.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveCadastrarComResidenciaInexistente() {
        when(categorias.buscarPorId("c1")).thenReturn(new Categoria("c1", "Laticínios", 3, null));
        when(residencias.buscarPorId("r1")).thenThrow(new ResidenciaNaoEncontradaException("r1"));

        assertThrows(ResidenciaNaoEncontradaException.class, () -> service.cadastrar(request("Geladeira")));
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveCadastrarComCategoriaInexistente() {
        when(categorias.buscarPorId("c1")).thenThrow(new CategoriaNaoEncontradaException("c1"));

        assertThrows(CategoriaNaoEncontradaException.class, () -> service.cadastrar(request("Geladeira")));
        verify(repository, never()).save(any());
    }

    @Test
    void deveAtualizarMantendoOId() {
        when(repository.findById("a1")).thenReturn(Optional.of(alimentoSalvo("a1")));
        referenciasExistem();
        salvarDevolvendoOMesmoObjeto();
        AlimentoRequest novosDados = new AlimentoRequest(
                "Leite integral", "r1", "c1", "Geladeira", new BigDecimal("1"), "L", LocalDate.of(2026, 11, 25));

        Alimento atualizado = service.atualizar("a1", novosDados);

        assertEquals("a1", atualizado.getId());
        assertEquals("Leite integral", atualizado.getNome());
        assertEquals(new BigDecimal("1"), atualizado.getQuantidade());
        assertEquals(LocalDate.of(2026, 11, 25), atualizado.getDataValidade());
    }

    @Test
    void deveManterOHistoricoAoAtualizar() {
        Alimento salvo = alimentoSalvo("a1");
        salvo.registrarMovimentacao(new Movimentacao(
                TipoMovimentacao.CONSUMO, BigDecimal.ONE, LocalDate.of(2026, 11, 9), null));
        when(repository.findById("a1")).thenReturn(Optional.of(salvo));
        referenciasExistem();
        salvarDevolvendoOMesmoObjeto();

        Alimento atualizado = service.atualizar("a1", request("Geladeira"));

        assertEquals(1, atualizado.getMovimentacoes().size());
    }

    @Test
    void deveRegistrarMovimentacaoComADataDeHojeQuandoNaoInformada() {
        when(repository.findById("a1")).thenReturn(Optional.of(alimentoSalvo("a1")));
        salvarDevolvendoOMesmoObjeto();

        Alimento atualizado = service.registrarMovimentacao(
                "a1", new MovimentacaoRequest(TipoMovimentacao.CONSUMO, new BigDecimal("0.5"), null, "Café da manhã"));

        assertEquals(new BigDecimal("1.5"), atualizado.getQuantidade());
        Movimentacao registrada = atualizado.getMovimentacoes().get(0);
        assertEquals(LocalDate.of(2026, 11, 10), registrada.data());
        assertEquals("Café da manhã", registrada.observacao());
    }

    @Test
    void deveRespeitarADataInformadaNaMovimentacao() {
        when(repository.findById("a1")).thenReturn(Optional.of(alimentoSalvo("a1")));
        salvarDevolvendoOMesmoObjeto();
        LocalDate ontem = LocalDate.of(2026, 11, 9);

        Alimento atualizado = service.registrarMovimentacao(
                "a1", new MovimentacaoRequest(TipoMovimentacao.DESCARTE, BigDecimal.ONE, ontem, null));

        assertEquals(ontem, atualizado.getMovimentacoes().get(0).data());
    }

    @Test
    void naoDeveSalvarMovimentacaoMaiorQueOEstoque() {
        when(repository.findById("a1")).thenReturn(Optional.of(alimentoSalvo("a1")));
        MovimentacaoRequest demais = new MovimentacaoRequest(TipoMovimentacao.DOACAO, BigDecimal.TEN, null, null);

        assertThrows(RegraDeNegocioException.class, () -> service.registrarMovimentacao("a1", demais));
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveRegistrarMovimentacaoEmAlimentoInexistente() {
        when(repository.findById("999")).thenReturn(Optional.empty());
        MovimentacaoRequest consumo = new MovimentacaoRequest(TipoMovimentacao.CONSUMO, BigDecimal.ONE, null, null);

        assertThrows(AlimentoNaoEncontradoException.class, () -> service.registrarMovimentacao("999", consumo));
    }

    @Test
    void deveListarAsMovimentacoesDoAlimento() {
        Alimento salvo = alimentoSalvo("a1");
        salvo.registrarMovimentacao(new Movimentacao(
                TipoMovimentacao.REPOSICAO, BigDecimal.ONE, LocalDate.of(2026, 11, 9), null));
        when(repository.findById("a1")).thenReturn(Optional.of(salvo));

        List<Movimentacao> historico = service.listarMovimentacoes("a1");

        assertEquals(1, historico.size());
        assertEquals(TipoMovimentacao.REPOSICAO, historico.get(0).tipo());
    }

    @Test
    void naoDeveAtualizarAlimentoInexistente() {
        when(repository.findById("999")).thenReturn(Optional.empty());

        assertThrows(AlimentoNaoEncontradoException.class, () -> service.atualizar("999", request("Geladeira")));
        verify(repository, never()).save(any());
    }

    @Test
    void deveListarOsAlimentosDaResidenciaPelaValidade() {
        when(residencias.buscarPorId("r1")).thenReturn(casaComGeladeira());
        when(repository.findByResidenciaIdOrderByDataValidadeAsc("r1")).thenReturn(List.of(alimentoSalvo("a1")));

        List<Alimento> daResidencia = service.listarPorResidencia("r1");

        assertEquals(1, daResidencia.size());
        assertEquals("a1", daResidencia.get(0).getId());
    }

    @Test
    void naoDeveListarAlimentosDeResidenciaInexistente() {
        when(residencias.buscarPorId("999")).thenThrow(new ResidenciaNaoEncontradaException("999"));

        assertThrows(ResidenciaNaoEncontradaException.class, () -> service.listarPorResidencia("999"));
    }

    @Test
    void deveBuscarListarEExcluirAlimentos() {
        Alimento alimento = alimentoSalvo("a1");
        when(repository.findById("a1")).thenReturn(Optional.of(alimento));
        when(repository.findAll()).thenReturn(List.of(alimento));
        when(repository.existsById("a1")).thenReturn(true);

        assertEquals(alimento, service.buscarPorId("a1"));
        assertEquals(List.of(alimento), service.listar());

        service.excluir("a1");
        verify(repository).deleteById("a1");
    }

    @Test
    void deveInformarQuandoAlimentoNaoForEncontrado() {
        when(repository.findById("999")).thenReturn(Optional.empty());

        AlimentoNaoEncontradoException erro =
                assertThrows(AlimentoNaoEncontradoException.class, () -> service.buscarPorId("999"));

        assertEquals("Alimento não encontrado: 999", erro.getMessage());
    }
}
