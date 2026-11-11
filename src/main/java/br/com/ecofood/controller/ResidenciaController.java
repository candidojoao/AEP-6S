package br.com.ecofood.controller;

import br.com.ecofood.dto.ResidenciaRequest;
import br.com.ecofood.model.LocalArmazenamento;
import br.com.ecofood.model.Residencia;
import br.com.ecofood.service.ResidenciaService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/residencias")
@Tag(name = "Residências", description = "Casas que guardam os alimentos e seus locais de armazenamento")
public class ResidenciaController {

    private final ResidenciaService service;

    public ResidenciaController(ResidenciaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastra uma residência com endereço e locais de armazenamento")
    public ResponseEntity<Residencia> cadastrar(@Valid @RequestBody ResidenciaRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.cadastrar(request));
    }

    @GetMapping
    @Operation(summary = "Lista as residências")
    public List<Residencia> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma residência pelo id")
    public Residencia buscarPorId(@PathVariable String id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma residência")
    public Residencia atualizar(@PathVariable String id, @Valid @RequestBody ResidenciaRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma residência")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/locais")
    @Operation(summary = "Adiciona um local de armazenamento à residência")
    public ResponseEntity<Residencia> adicionarLocal(
            @PathVariable String id,
            @Valid @RequestBody LocalArmazenamento local
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.adicionarLocal(id, local));
    }
}
