package br.com.ecofood.controller;

import br.com.ecofood.model.Alimento;
import br.com.ecofood.service.AlimentoService;
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
@RequestMapping("/alimentos")
public class AlimentoController {

    private final AlimentoService service;

    public AlimentoController(AlimentoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Alimento> cadastrar(@Valid @RequestBody Alimento alimento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(alimento));
    }

    @GetMapping
    public List<Alimento> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Alimento buscarPorId(@PathVariable String id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Alimento atualizar(@PathVariable String id, @Valid @RequestBody Alimento alimento) {
        return service.atualizar(id, alimento);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

