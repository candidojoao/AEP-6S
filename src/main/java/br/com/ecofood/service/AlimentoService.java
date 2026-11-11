package br.com.ecofood.service;

import br.com.ecofood.dto.AlimentoRequest;
import br.com.ecofood.exception.AlimentoNaoEncontradoException;
import br.com.ecofood.exception.RecursoNaoEncontradoException;
import br.com.ecofood.model.Alimento;
import br.com.ecofood.repository.AlimentoRepository;
import org.springframework.stereotype.Service;

@Service
public class AlimentoService extends ServicoCrud<Alimento> {

    private final AlimentoRepository alimentos;

    public AlimentoService(AlimentoRepository alimentos) {
        super(alimentos);
        this.alimentos = alimentos;
    }

    public Alimento cadastrar(AlimentoRequest request) {
        return alimentos.save(request.paraAlimento(null));
    }

    public Alimento atualizar(String id, AlimentoRequest request) {
        garantirQueExiste(id);
        return alimentos.save(request.paraAlimento(id));
    }

    @Override
    protected RecursoNaoEncontradoException naoEncontrado(String id) {
        return new AlimentoNaoEncontradoException(id);
    }
}
