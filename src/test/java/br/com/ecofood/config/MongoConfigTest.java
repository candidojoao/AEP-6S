package br.com.ecofood.config;

import org.bson.types.Decimal128;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoExceptionTranslator;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MongoConfigTest {

    private final MongoConfig config = new MongoConfig();

    @Test
    void deveGravarBigDecimalComoDecimal128() {
        MongoCustomConversions conversoes = config.mongoCustomConversions();

        Optional<Class<?>> destino = conversoes.getCustomWriteTarget(BigDecimal.class);

        assertEquals(Optional.of(Decimal128.class), destino);
    }

    @Test
    void naoDeveGravarOCampoClassNosDocumentos() {
        MongoDatabaseFactory fabrica = mock(MongoDatabaseFactory.class);
        when(fabrica.getExceptionTranslator()).thenReturn(new MongoExceptionTranslator());

        MappingMongoConverter conversor = config.mappingMongoConverter(
                fabrica,
                new MongoMappingContext(),
                config.mongoCustomConversions()
        );

        assertFalse(conversor.getTypeMapper().isTypeKey("_class"));
        assertEquals(Optional.of(Decimal128.class),
                conversor.getCustomConversions().getCustomWriteTarget(BigDecimal.class));
    }
}
