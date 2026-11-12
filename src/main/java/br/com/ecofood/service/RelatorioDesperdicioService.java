package br.com.ecofood.service;

import br.com.ecofood.dto.DesperdicioPorUnidade;
import br.com.ecofood.dto.RelatorioDesperdicio;
import br.com.ecofood.dto.TotalMovimentado;
import br.com.ecofood.model.Residencia;
import br.com.ecofood.repository.RelatorioDesperdicioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class RelatorioDesperdicioService {

    private static final int ITENS_MAIS_DESCARTADOS = 5;

    private final ResidenciaService residencias;
    private final RelatorioDesperdicioRepository relatorio;

    public RelatorioDesperdicioService(ResidenciaService residencias, RelatorioDesperdicioRepository relatorio) {
        this.residencias = residencias;
        this.relatorio = relatorio;
    }

    public RelatorioDesperdicio gerar(String residenciaId) {
        Residencia residencia = residencias.buscarPorId(residenciaId);

        Map<String, List<TotalMovimentado>> totaisPorUnidade = relatorio.totaisPorUnidadeETipo(residenciaId)
                .stream()
                .collect(Collectors.groupingBy(TotalMovimentado::unidade, TreeMap::new, Collectors.toList()));

        List<DesperdicioPorUnidade> porUnidade = totaisPorUnidade.entrySet().stream()
                .map(entrada -> DesperdicioPorUnidade.de(entrada.getKey(), entrada.getValue()))
                .toList();

        return new RelatorioDesperdicio(
                residencia.getId(),
                residencia.getNome(),
                porUnidade,
                relatorio.maisDescartados(residenciaId, ITENS_MAIS_DESCARTADOS)
        );
    }
}
