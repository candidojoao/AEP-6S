package br.com.ecofood.service;

import br.com.ecofood.exception.RecursoNaoEncontradoException;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public abstract class ServicoCrud<T> {

    private final MongoRepository<T, String> repositorio;

    protected ServicoCrud(MongoRepository<T, String> repositorio) {
        this.repositorio = repositorio;
    }

    public List<T> listar() {
        return repositorio.findAll();
    }

    public T buscarPorId(String id) {
        return repositorio.findById(id)
                .orElseThrow(() -> naoEncontrado(id));
    }

    public void excluir(String id) {
        garantirQueExiste(id);
        repositorio.deleteById(id);
    }

    protected void garantirQueExiste(String id) {
        if (!repositorio.existsById(id)) {
            throw naoEncontrado(id);
        }
    }

    protected abstract RecursoNaoEncontradoException naoEncontrado(String id);
}
