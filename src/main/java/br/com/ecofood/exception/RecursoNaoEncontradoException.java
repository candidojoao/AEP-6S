package br.com.ecofood.exception;

public abstract class RecursoNaoEncontradoException extends RuntimeException {

    protected RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
