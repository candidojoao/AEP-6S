package br.com.ecofood.exception;

public class CategoriaNaoEncontradaException extends RecursoNaoEncontradoException {

    public CategoriaNaoEncontradaException(String id) {
        super("Categoria não encontrada: " + id);
    }
}
