package br.com.ecofood.exception;

import br.com.ecofood.exception.ApiExceptionHandler.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void deveResponder404ParaQualquerRecursoNaoEncontrado() {
        ResponseEntity<ApiError> resposta =
                handler.tratarNaoEncontrado(new AlimentoNaoEncontradoException("123"));

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
        assertEquals("Alimento não encontrado: 123", resposta.getBody().mensagem());
    }

    @Test
    void deveResponder422ParaRegraDeNegocio() {
        ResponseEntity<ApiError> resposta =
                handler.tratarRegraDeNegocio(new RegraDeNegocioException("Regra violada"));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, resposta.getStatusCode());
        assertEquals(422, resposta.getBody().status());
        assertEquals("Regra violada", resposta.getBody().mensagem());
        assertTrue(resposta.getBody().campos().isEmpty());
    }

    @Test
    void deveResponder409ParaRegistroDuplicado() {
        ResponseEntity<ApiError> resposta = handler.tratarDuplicidade();

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertEquals("Já existe um registro com esses dados", resposta.getBody().mensagem());
    }
}
