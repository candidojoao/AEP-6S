package br.com.ecofood.integracao;

import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

abstract class BancoEmbutido {

    private static final int PORTA_DO_MONGODB_INSTALADO = 27017;

    @Autowired
    protected MongoTemplate mongo;

    @Autowired
    private MongoClient cliente;

    @BeforeEach
    void limparBancoEmbutido() {
        int porta = cliente.getClusterDescription().getClusterSettings().getHosts().get(0).getPort();
        assertNotEquals(PORTA_DO_MONGODB_INSTALADO, porta,
                "Os testes de integração precisam usar o MongoDB embutido, nunca o banco instalado");

        mongo.getCollectionNames()
                .forEach(colecao -> mongo.getCollection(colecao).deleteMany(new Document()));
    }
}
