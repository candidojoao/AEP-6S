package br.com.ecofood.repository;

import br.com.ecofood.model.Alimento;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AlimentoRepository extends MongoRepository<Alimento, String> {

    List<Alimento> findByResidenciaIdOrderByDataValidadeAsc(String residenciaId);

    boolean existsByResidenciaId(String residenciaId);

    boolean existsByCategoriaId(String categoriaId);
}
