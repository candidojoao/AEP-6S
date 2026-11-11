package br.com.ecofood.exception;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiError> tratarNaoEncontrado(RecursoNaoEncontradoException exception) {
        return resposta(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ApiError> tratarRegraDeNegocio(RegraDeNegocioException exception) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiError> tratarDuplicidade() {
        return resposta(HttpStatus.CONFLICT, "Já existe um registro com esses dados", Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> tratarValidacao(MethodArgumentNotValidException exception) {
        Map<String, String> campos = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));

        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> tratarCorpoInvalido() {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido", Map.of());
    }

    private ResponseEntity<ApiError> resposta(HttpStatus status, String mensagem, Map<String, String> campos) {
        ApiError erro = new ApiError(Instant.now(), status.value(), mensagem, campos);
        return ResponseEntity.status(status).body(erro);
    }

    public record ApiError(
            Instant timestamp,
            int status,
            String mensagem,
            Map<String, String> campos
    ) {
    }
}
