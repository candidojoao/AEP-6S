package br.com.ecofood.config;

import org.bson.types.Decimal128;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MongoConfigTest {

    @Test
    void deveGravarBigDecimalComoDecimal128() {
        MongoCustomConversions conversoes = new MongoConfig().mongoCustomConversions();

        Optional<Class<?>> destino = conversoes.getCustomWriteTarget(BigDecimal.class);

        assertEquals(Optional.of(Decimal128.class), destino);
    }
}
