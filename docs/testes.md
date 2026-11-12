# Testes

O projeto tem **133 testes automatizados**, escritos com JUnit 5. A cobertura é medida pelo JaCoCo, e o build falha se ela ficar abaixo de 70% das linhas.

## Como rodar

| Comando (Windows) | Comando (Linux e macOS) | O que roda | Resultado |
|---|---|---|---|
| `mvnw.cmd clean verify` | `./mvnw clean verify` | testes de domínio, serviços e API | 120 testes, 94,0% das linhas |
| `mvnw.cmd clean verify -Pintegracao` | `./mvnw clean verify -Pintegracao` | todos os testes, incluindo os com MongoDB embutido | 133 testes, 99,5% das linhas |

- Os dois comandos usam o Maven Wrapper, então não é preciso ter o Maven instalado. Também não é preciso ter o MongoDB instalado.
- O perfil `integracao` baixa o MongoDB 8.0.4 na primeira execução (cerca de 740 MB no Windows e 95 MB no Linux) e o reaproveita nas seguintes. Por isso ele não é o comando padrão.
- O relatório de cobertura fica em `target/site/jacoco/index.html`. Abra no navegador para ver a cobertura por pacote, classe e linha.

## Tipos de teste

| Tipo | Quantidade | Ferramentas | O que verifica |
|---|---|---|---|
| Domínio | 26 | JUnit | regras dentro das entidades: estoque, locais repetidos, dias para vencer, alerta por categoria, situação da validade |
| DTOs | 4 | JUnit | cálculo do aproveitamento e arredondamento do relatório |
| Serviços | 42 | JUnit + Mockito | casos de uso com o banco simulado: referências entre coleções, travas de exclusão, histórico mantido na atualização, data padrão das movimentações (com relógio fixo), montagem dos alertas e do relatório |
| API | 41 | MockMvc (`@WebMvcTest`) | status HTTP, JSON de resposta, validação dos campos (inclusive dentro de subdocumentos) e formato dos erros |
| Configuração e erros | 7 | JUnit + Mockito | conversão `BigDecimal` → `Decimal128`, conversor sem `_class`, fuso de Brasília, descrição do Swagger e tradução das exceções |
| Integração | 13 | `@DataMongoTest` + MongoDB embutido | o que só um MongoDB de verdade prova: subdocumentos gravados, referências como `ObjectId`, `Decimal128`, consultas derivadas, índice único, leitura de documentos da 1ª entrega e as agregações do relatório |

## Testes de integração

Ficam no pacote `br.com.ecofood.integracao` e usam a anotação `@TesteDeIntegracaoMongo`, que reúne:

- `@DataMongoTest`, que sobe só a parte de persistência do Spring;
- o MongoDB embutido do Flapdoodle, iniciado numa porta aleatória;
- a configuração real do projeto (`MongoConfig`), para gravar os mesmos tipos da aplicação;
- a tag `integracao`, usada pelo perfil Maven.

Antes de cada teste, a classe `BancoEmbutido` **confere que a conexão não está na porta 27017**, a do MongoDB instalado na máquina. Só depois apaga os documentos. Assim, um erro de configuração nunca apaga dados reais.

| Classe | Testes | Exemplos |
|---|---|---|
| `RepositoriosIntegracaoTest` | 9 | endereço e locais gravados como subdocumentos e sem `_class`; referências como `ObjectId`; alimentos da residência por ordem de validade; índice único barrando categoria repetida; leitura de um alimento no formato da v1 |
| `RelatorioDesperdicioIntegracaoTest` | 4 | soma por unidade e tipo só da residência pedida; reposição fora da soma; mais descartados em ordem decrescente; aproveitamento calculado de ponta a ponta |

## Cobertura por pacote

Resultado do comando completo (`-Pintegracao`):

| Pacote | Linhas cobertas |
|---|---|
| `config` | 100% (14/14) |
| `controller` | 100% (48/48) |
| `dto` | 100% (37/37) |
| `exception` | 100% (22/22) |
| `model` | 100% (101/101) |
| `repository` | 100% (24/24) |
| `service` | 100% (115/115) |
| `br.com.ecofood` (classe `EcoFoodApplication`) | 1 de 3 linhas: o `main` só roda ao iniciar a aplicação |
| **Total** | **99,5% (362/364)** |

## Convenções

- Os nomes dos testes descrevem o comportamento esperado em português (`naoDeveTirarMaisDoQueOEstoque`, `deveManterOHistoricoAoAtualizar`).
- Cada teste segue a ordem preparar → executar → verificar.
- Regras de data usam um `Clock` fixo, para o resultado não depender do dia em que o teste roda.
