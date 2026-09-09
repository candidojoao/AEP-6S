package br.com.ecofood.exception;

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

    @ExceptionHandler(AlimentoNaoEncontradoException.class)
    public ResponseEntity<ApiError> tratarNaoEncontrado(AlimentoNaoEncontradoException exception) {
        ApiError erro = new ApiError(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> tratarValidacao(MethodArgumentNotValidException exception) {
        Map<String, String> campos = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));

        ApiError erro = new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Dados inválidos",
                campos
        );
        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> tratarCorpoInvalido() {
        ApiError erro = new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Corpo da requisição inválido",
                Map.of()
        );
        return ResponseEntity.badRequest().body(erro);
    }

    public record ApiError(
            Instant timestamp,
            int status,
            String mensagem,
            Map<String, String> campos
    ) {
    }
}
