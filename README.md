# EcoFood

O EcoFood é uma API REST desenvolvida como Prova de Conceito (PoC) para a AEP do curso de Engenharia de Software.

A aplicação permite o cadastro e o gerenciamento de alimentos armazenados, possibilitando controlar informações como nome, categoria, quantidade, unidade de medida e data de validade.

## Problema

O desperdício de alimentos pode ocorrer pela falta de controle dos produtos armazenados, principalmente em relação à quantidade disponível e às datas de validade.

O EcoFood busca auxiliar nesse controle permitindo cadastrar, consultar, atualizar e excluir alimentos, facilitando o acompanhamento dos produtos disponíveis e contribuindo para a redução do desperdício.

## Público-alvo

A solução é voltada a *famílias e residências* que precisam acompanhar os alimentos guardados em casa.

Esse público costuma não ter nenhum registro do que possui: os alimentos ficam espalhados entre despensa, geladeira e freezer, e a validade só é percebida quando o produto já venceu. O EcoFood atende esse contexto oferecendo um registro simples e consultável do que está armazenado, com quantidade e data de validade.

O mesmo modelo se aplica a pequenos estabelecimentos de alimentação, que enfrentam o problema em escala maior.

## ODS 12 — Consumo e Produção Responsáveis

O projeto está relacionado ao Objetivo de Desenvolvimento Sustentável 12 (ODS 12) da Organização das Nações Unidas — Consumo e Produção Responsáveis.

O EcoFood contribui com esse objetivo ao permitir um melhor controle dos alimentos armazenados e de suas datas de validade, auxiliando na identificação dos produtos disponíveis e reduzindo a possibilidade de desperdício por falta de acompanhamento.

## Funcionalidades

Nesta primeira versão, a aplicação disponibiliza as seguintes funcionalidades:

- cadastro de alimentos;
- listagem dos alimentos cadastrados;
- consulta de alimento pelo ID;
- atualização de alimento;
- exclusão de alimento.

Os alimentos são armazenados em uma coleção MongoDB chamada alimentos.

## Estrutura

text
AlimentoRequest (DTO)
↓
Controller → Service → Repository → MongoDB
↓
ApiExceptionHandler


As requisições de escrita são recebidas em um DTO (AlimentoRequest), e não na entidade de persistência. O id nunca é aceito pelo corpo da requisição: no cadastro ele é gerado pelo MongoDB e na atualização é lido da URL.

O ApiExceptionHandler centraliza o tratamento de erros e padroniza as respostas 404 e 400.

## Tecnologias

- Java 21
- Spring Boot 3
- Spring Data MongoDB
- MongoDB
- Maven
- JUnit 5, Mockito e JaCoCo

## Banco de dados

O projeto utiliza MongoDB como banco de dados NoSQL.

Banco:
- ecofood

Coleção:
- alimentos

A aplicação utiliza apenas a coleção alimentos, com documentos homogêneos e estrutura simples, conforme o escopo previsto para o 1º semestre.

Exemplo de documento armazenado:

json
{
"_id": "6aa0b89f2140541683daa0b4",
"nome": "Arroz",
"categoria": "Graos",
"quantidade": 5,
"unidade": "kg",
"dataValidade": "2026-10-15"
}

text
mongodb://localhost:27017/ecofood

Também é possível configurar outra conexão por meio da variável de ambiente MONGODB_URI.

## Como executar

Tenha o Java 21, o Maven e o MongoDB instalados.

Inicie a API:

bash
mvn spring-boot:run


A API ficará disponível em http://localhost:8080.

## Endpoints

| Método | Caminho | Resultado |
|---|---|---|
| POST | /alimentos | Cadastra um alimento (201) |
| GET | /alimentos | Lista os alimentos (200) |
| GET | /alimentos/{id} | Busca um alimento (200 ou 404) |
| PUT | /alimentos/{id} | Atualiza um alimento (200 ou 404) |
| DELETE | /alimentos/{id} | Exclui um alimento (204 ou 404) |

Exemplo de cadastro:

bash
curl -X POST http://localhost:8080/alimentos \
-H "Content-Type: application/json" \
-d '{
"nome": "Arroz",
"categoria": "Grãos",
"quantidade": 5,
"unidade": "kg",
"dataValidade": "2026-10-15"
}'

O id é gerado pelo MongoDB. A data deve usar o formato YYYY-MM-DD, e a quantidade deve ser maior que zero.

Erros de validação retornam 400 com os campos rejeitados:

json
{
"timestamp": "2026-09-09T21:00:00Z",
"status": 400,
"mensagem": "Dados inválidos",
"campos": {
"nome": "O nome é obrigatório"
}
}


## Testes e cobertura

Execute os testes e valide a cobertura:

bash
mvn clean verify


Resultado obtido nesta versão:

| Métrica | Valor |
|---|---|
| Testes executados | 18 (0 falhas) |
| Cobertura de linhas | 97,5% (77/79) |
| Cobertura de instruções | 98% |
| Mínimo exigido | 70% |

O relatório HTML é gerado em target/site/jacoco/index.html.

O build falha automaticamente se a cobertura de linhas ficar abaixo de 70%.

## Equipe

- André Mulati
- João Paulo Candido
- Pedro Carnelossi