package br.com.ecofood.repository;

import br.com.ecofood.model.Residencia;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResidenciaRepository extends MongoRepository<Residencia, String> {
}
