package br.com.ecofood.exception;

public class ResidenciaNaoEncontradaException extends RecursoNaoEncontradoException {

    public ResidenciaNaoEncontradaException(String id) {
        super("Residência não encontrada: " + id);
    }
}
