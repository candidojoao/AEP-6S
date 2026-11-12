package br.com.ecofood.integracao;

import br.com.ecofood.config.MongoConfig;
import br.com.ecofood.repository.RelatorioDesperdicioRepository;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag("integracao")
@DataMongoTest(properties = "de.flapdoodle.mongodb.embedded.version=8.0.4")
@Import({MongoConfig.class, RelatorioDesperdicioRepository.class})
public @interface TesteDeIntegracaoMongo {
}
