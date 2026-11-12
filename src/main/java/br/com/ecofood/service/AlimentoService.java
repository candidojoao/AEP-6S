package br.com.ecofood.service;

import br.com.ecofood.dto.AlimentoRequest;
import br.com.ecofood.dto.MovimentacaoRequest;
import br.com.ecofood.exception.AlimentoNaoEncontradoException;
import br.com.ecofood.exception.RecursoNaoEncontradoException;
import br.com.ecofood.exception.RegraDeNegocioException;
import br.com.ecofood.model.Alimento;
import br.com.ecofood.model.LocalArmazenamento;
import br.com.ecofood.model.Movimentacao;
import br.com.ecofood.model.Residencia;
import br.com.ecofood.repository.AlimentoRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class AlimentoService extends ServicoCrud<Alimento> {

    private final AlimentoRepository alimentos;
    private final ResidenciaService residencias;
    private final CategoriaService categorias;
    private final Clock relogio;

    public AlimentoService(
            AlimentoRepository alimentos,
            ResidenciaService residencias,
            CategoriaService categorias,
            Clock relogio
    ) {
        super(alimentos);
        this.alimentos = alimentos;
        this.residencias = residencias;
        this.categorias = categorias;
        this.relogio = relogio;
    }

    public List<Alimento> listarPorResidencia(String residenciaId) {
        residencias.buscarPorId(residenciaId);
        return alimentos.findByResidenciaIdOrderByDataValidadeAsc(residenciaId);
    }

    public Alimento cadastrar(AlimentoRequest request) {
        return alimentos.save(montarComReferenciasConferidas(request));
    }

    public Alimento atualizar(String id, AlimentoRequest request) {
        Alimento existente = buscarPorId(id);
        existente.atualizarCadastro(montarComReferenciasConferidas(request));
        return alimentos.save(existente);
    }

    public Alimento registrarMovimentacao(String id, MovimentacaoRequest request) {
        Alimento alimento = buscarPorId(id);
        alimento.registrarMovimentacao(request.paraMovimentacao(LocalDate.now(relogio)));
        return alimentos.save(alimento);
    }

    public List<Movimentacao> listarMovimentacoes(String id) {
        return buscarPorId(id).getMovimentacoes();
    }

    private Alimento montarComReferenciasConferidas(AlimentoRequest request) {
        categorias.buscarPorId(request.categoriaId());
        Residencia residencia = residencias.buscarPorId(request.residenciaId());
        LocalArmazenamento local = residencia.buscarLocal(request.local())
                .orElseThrow(() -> new RegraDeNegocioException(
                        "A residência " + residencia.getNome() + " não possui o local: " + request.local()));
        return request.paraAlimento(local.nome());
    }

    @Override
    protected RecursoNaoEncontradoException naoEncontrado(String id) {
        return new AlimentoNaoEncontradoException(id);
    }
}
