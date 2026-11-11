package br.com.ecofood.service;

import br.com.ecofood.dto.ResidenciaRequest;
import br.com.ecofood.exception.RecursoNaoEncontradoException;
import br.com.ecofood.exception.ResidenciaNaoEncontradaException;
import br.com.ecofood.model.LocalArmazenamento;
import br.com.ecofood.model.Residencia;
import br.com.ecofood.repository.ResidenciaRepository;
import org.springframework.stereotype.Service;

@Service
public class ResidenciaService extends ServicoCrud<Residencia> {

    private final ResidenciaRepository residencias;

    public ResidenciaService(ResidenciaRepository residencias) {
        super(residencias);
        this.residencias = residencias;
    }

    public Residencia cadastrar(ResidenciaRequest request) {
        return residencias.save(request.paraResidencia(null));
    }

    public Residencia atualizar(String id, ResidenciaRequest request) {
        garantirQueExiste(id);
        return residencias.save(request.paraResidencia(id));
    }

    public Residencia adicionarLocal(String id, LocalArmazenamento local) {
        Residencia residencia = buscarPorId(id);
        residencia.adicionarLocal(local);
        return residencias.save(residencia);
    }

    @Override
    protected RecursoNaoEncontradoException naoEncontrado(String id) {
        return new ResidenciaNaoEncontradaException(id);
    }
}
