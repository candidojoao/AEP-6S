package br.com.ecofood.exception;

public class AlimentoNaoEncontradoException extends RecursoNaoEncontradoException {

    public AlimentoNaoEncontradoException(String id) {
        super("Alimento não encontrado: " + id);
    }
}
