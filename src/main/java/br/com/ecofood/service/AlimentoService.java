package br.com.ecofood.service;

import br.com.ecofood.exception.AlimentoNaoEncontradoException;
import br.com.ecofood.model.Alimento;
import br.com.ecofood.repository.AlimentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlimentoService {

    private final AlimentoRepository repository;

    public AlimentoService(AlimentoRepository repository) {
        this.repository = repository;
    }

    public Alimento cadastrar(Alimento alimento) {
        Alimento novoAlimento = new Alimento(
                null,
                alimento.nome(),
                alimento.categoria(),
                alimento.quantidade(),
                alimento.unidade(),
                alimento.dataValidade()
        );
        return repository.save(novoAlimento);
    }

    public List<Alimento> listar() {
        return repository.findAll();
    }

    public Alimento buscarPorId(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new AlimentoNaoEncontradoException(id));
    }

    public Alimento atualizar(String id, Alimento alimento) {
        if (!repository.existsById(id)) {
            throw new AlimentoNaoEncontradoException(id);
        }

        Alimento alimentoAtualizado = new Alimento(
                id,
                alimento.nome(),
                alimento.categoria(),
                alimento.quantidade(),
                alimento.unidade(),
                alimento.dataValidade()
        );
        return repository.save(alimentoAtualizado);
    }

    public void excluir(String id) {
        if (!repository.existsById(id)) {
            throw new AlimentoNaoEncontradoException(id);
        }
        repository.deleteById(id);
    }
}

