package br.com.ecofood.repository;

import br.com.ecofood.model.Alimento;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AlimentoRepository extends MongoRepository<Alimento, String> {
}

