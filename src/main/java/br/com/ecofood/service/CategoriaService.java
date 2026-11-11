package br.com.ecofood.service;

import br.com.ecofood.dto.CategoriaRequest;
import br.com.ecofood.exception.CategoriaNaoEncontradaException;
import br.com.ecofood.exception.RecursoNaoEncontradoException;
import br.com.ecofood.exception.RegraDeNegocioException;
import br.com.ecofood.model.Categoria;
import br.com.ecofood.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoriaService extends ServicoCrud<Categoria> {

    private final CategoriaRepository categorias;

    public CategoriaService(CategoriaRepository categorias) {
        super(categorias);
        this.categorias = categorias;
    }

    public Categoria cadastrar(CategoriaRequest request) {
        garantirNomeDisponivel(request.nome(), null);
        return categorias.save(request.paraCategoria(null));
    }

    public Categoria atualizar(String id, CategoriaRequest request) {
        garantirQueExiste(id);
        garantirNomeDisponivel(request.nome(), id);
        return categorias.save(request.paraCategoria(id));
    }

    private void garantirNomeDisponivel(String nome, String idAtual) {
        categorias.findByNomeIgnoreCase(nome.trim())
                .filter(existente -> !existente.getId().equals(idAtual))
                .ifPresent(existente -> {
                    throw new RegraDeNegocioException(
                            "Já existe uma categoria com o nome: " + existente.getNome());
                });
    }

    @Override
    protected RecursoNaoEncontradoException naoEncontrado(String id) {
        return new CategoriaNaoEncontradaException(id);
    }
}
