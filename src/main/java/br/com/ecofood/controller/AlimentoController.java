package br.com.ecofood.controller;

import br.com.ecofood.dto.AlimentoRequest;
import br.com.ecofood.model.Alimento;
import br.com.ecofood.service.AlimentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alimentos")
@Tag(name = "Alimentos", description = "Alimentos armazenados")
public class AlimentoController {

    private final AlimentoService service;

    public AlimentoController(AlimentoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastra um alimento")
    public ResponseEntity<Alimento> cadastrar(@Valid @RequestBody AlimentoRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.cadastrar(request));
    }

    @GetMapping
    @Operation(summary = "Lista os alimentos, opcionalmente só os de uma residência (ordenados pela validade)")
    public List<Alimento> listar(
            @Parameter(description = "Id da residência") @RequestParam(required = false) String residenciaId
    ) {
        if (residenciaId == null || residenciaId.isBlank()) {
            return service.listar();
        }
        return service.listarPorResidencia(residenciaId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um alimento pelo id")
    public Alimento buscarPorId(@PathVariable String id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um alimento")
    public Alimento atualizar(@PathVariable String id, @Valid @RequestBody AlimentoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um alimento")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
