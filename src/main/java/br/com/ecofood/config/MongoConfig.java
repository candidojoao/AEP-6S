package br.com.ecofood.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions.BigDecimalRepresentation;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return MongoCustomConversions.create(
                adapter -> adapter.bigDecimal(BigDecimalRepresentation.DECIMAL128)
        );
    }

    @Bean
    public MappingMongoConverter mappingMongoConverter(
            MongoDatabaseFactory fabrica,
            MongoMappingContext contexto,
            MongoCustomConversions conversoes
    ) {
        MappingMongoConverter conversor =
                new MappingMongoConverter(new DefaultDbRefResolver(fabrica), contexto);
        conversor.setCustomConversions(conversoes);
        conversor.setTypeMapper(new DefaultMongoTypeMapper(null));
        return conversor;
    }
}
