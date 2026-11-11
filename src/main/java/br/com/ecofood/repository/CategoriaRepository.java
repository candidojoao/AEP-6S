package br.com.ecofood.repository;

import br.com.ecofood.model.Categoria;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CategoriaRepository extends MongoRepository<Categoria, String> {

    Optional<Categoria> findByNomeIgnoreCase(String nome);
}
