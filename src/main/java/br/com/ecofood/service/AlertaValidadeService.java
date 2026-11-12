package br.com.ecofood.service;

import br.com.ecofood.dto.AlertaValidade;
import br.com.ecofood.model.Alimento;
import br.com.ecofood.model.Categoria;
import br.com.ecofood.repository.AlimentoRepository;
import br.com.ecofood.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AlertaValidadeService {

    private final ResidenciaService residencias;
    private final AlimentoRepository alimentos;
    private final CategoriaRepository categorias;
    private final Clock relogio;

    public AlertaValidadeService(
            ResidenciaService residencias,
            AlimentoRepository alimentos,
            CategoriaRepository categorias,
            Clock relogio
    ) {
        this.residencias = residencias;
        this.alimentos = alimentos;
        this.categorias = categorias;
        this.relogio = relogio;
    }

    public List<AlertaValidade> listar(String residenciaId) {
        residencias.buscarPorId(residenciaId);
        LocalDate hoje = LocalDate.now(relogio);

        List<Alimento> comEstoque = alimentos.findByResidenciaIdOrderByDataValidadeAsc(residenciaId).stream()
                .filter(Alimento::temEstoque)
                .toList();
        Map<String, Categoria> categoriasPorId = buscarCategorias(comEstoque);

        return comEstoque.stream()
                .filter(alimento -> categoriasPorId.containsKey(alimento.getCategoriaId()))
                .filter(alimento -> categoriasPorId.get(alimento.getCategoriaId())
                        .deveAlertar(alimento.diasParaVencer(hoje)))
                .map(alimento -> AlertaValidade.de(alimento, categoriasPorId.get(alimento.getCategoriaId()), hoje))
                .toList();
    }

    private Map<String, Categoria> buscarCategorias(List<Alimento> lista) {
        List<String> ids = lista.stream().map(Alimento::getCategoriaId).distinct().toList();
        return categorias.findAllById(ids).stream()
                .collect(Collectors.toMap(Categoria::getId, Function.identity()));
    }
}
