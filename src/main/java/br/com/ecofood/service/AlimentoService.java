package br.com.ecofood.service;

import br.com.ecofood.dto.AlimentoRequest;
import br.com.ecofood.exception.AlimentoNaoEncontradoException;
import br.com.ecofood.exception.RecursoNaoEncontradoException;
import br.com.ecofood.exception.RegraDeNegocioException;
import br.com.ecofood.model.Alimento;
import br.com.ecofood.model.LocalArmazenamento;
import br.com.ecofood.model.Residencia;
import br.com.ecofood.repository.AlimentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlimentoService extends ServicoCrud<Alimento> {

    private final AlimentoRepository alimentos;
    private final ResidenciaService residencias;
    private final CategoriaService categorias;

    public AlimentoService(
            AlimentoRepository alimentos,
            ResidenciaService residencias,
            CategoriaService categorias
    ) {
        super(alimentos);
        this.alimentos = alimentos;
        this.residencias = residencias;
        this.categorias = categorias;
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
