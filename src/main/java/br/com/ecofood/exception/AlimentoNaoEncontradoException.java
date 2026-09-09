package br.com.ecofood.exception;

public class AlimentoNaoEncontradoException extends RuntimeException {

    public AlimentoNaoEncontradoException(String id) {
        super("Alimento não encontrado: " + id);
    }
}

