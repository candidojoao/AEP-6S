package br.com.ecofood.service;

import br.com.ecofood.dto.ResidenciaRequest;
import br.com.ecofood.exception.RecursoNaoEncontradoException;
import br.com.ecofood.exception.RegraDeNegocioException;
import br.com.ecofood.exception.ResidenciaNaoEncontradaException;
import br.com.ecofood.model.Alimento;
import br.com.ecofood.model.LocalArmazenamento;
import br.com.ecofood.model.Residencia;
import br.com.ecofood.repository.AlimentoRepository;
import br.com.ecofood.repository.ResidenciaRepository;
import org.springframework.stereotype.Service;

@Service
public class ResidenciaService extends ServicoCrud<Residencia> {

    private final ResidenciaRepository residencias;
    private final AlimentoRepository alimentos;

    public ResidenciaService(ResidenciaRepository residencias, AlimentoRepository alimentos) {
        super(residencias);
        this.residencias = residencias;
        this.alimentos = alimentos;
    }

    public Residencia cadastrar(ResidenciaRequest request) {
        return residencias.save(request.paraResidencia(null));
    }

    public Residencia atualizar(String id, ResidenciaRequest request) {
        garantirQueExiste(id);
        Residencia atualizada = request.paraResidencia(id);
        garantirQueLocaisEmUsoContinuam(atualizada);
        return residencias.save(atualizada);
    }

    public Residencia adicionarLocal(String id, LocalArmazenamento local) {
        Residencia residencia = buscarPorId(id);
        residencia.adicionarLocal(local);
        return residencias.save(residencia);
    }

    private void garantirQueLocaisEmUsoContinuam(Residencia atualizada) {
        alimentos.findByResidenciaIdOrderByDataValidadeAsc(atualizada.getId()).stream()
                .map(Alimento::getLocal)
                .filter(local -> !atualizada.possuiLocal(local))
                .findFirst()
                .ifPresent(local -> {
                    throw new RegraDeNegocioException(
                            "O local " + local + " tem alimentos guardados e não pode ser removido");
                });
    }

    @Override
    protected void antesDeExcluir(String id) {
        if (alimentos.existsByResidenciaId(id)) {
            throw new RegraDeNegocioException(
                    "A residência tem alimentos cadastrados e não pode ser excluída");
        }
    }

    @Override
    protected RecursoNaoEncontradoException naoEncontrado(String id) {
        return new ResidenciaNaoEncontradaException(id);
    }
}
