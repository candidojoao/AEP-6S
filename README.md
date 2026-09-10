## EcoFood

O EcoFood é uma API REST desenvolvida como Prova de Conceito (PoC) para a AEP do curso de Engenharia de Software.

A aplicação permite o cadastro e o gerenciamento de alimentos armazenados, possibilitando controlar informações como nome, categoria, quantidade, unidade de medida e data de validade.

## Problema

O desperdício de alimentos pode ocorrer pela falta de controle dos produtos armazenados, principalmente em relação à quantidade disponível e às datas de validade.

O EcoFood busca auxiliar nesse controle permitindo cadastrar, consultar, atualizar e excluir alimentos, facilitando o acompanhamento dos produtos disponíveis e contribuindo para a redução do desperdício.

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
## Tecnologias

- Java 21
- Spring Boot 3
- Spring Data MongoDB
- MongoDB
- Maven
- JUnit 5, Mockito e JaCoCo

## Estrutura

```text
Controller → Service → Repository → MongoDB
```

A aplicação utiliza apenas a coleção `alimentos`, conforme o escopo da primeira entrega.

## Banco de dados

O projeto utiliza MongoDB como banco de dados NoSQL.

Banco:
- ecofood

Coleção:
- alimentos

Por padrão, a aplicação utiliza a seguinte conexão:
mongodb://localhost:27017/ecofood

Também é possível configurar outra conexão por meio da variável de ambiente:

MONGODB_URI

## Como executar

Tenha o Java 21, o Maven e o MongoDB instalados. Por padrão, a aplicação conecta em:

```text
mongodb://localhost:27017/ecofood
```

Para usar outra conexão, defina a variável `MONGODB_URI`.

Inicie a API:

```bash
mvn spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

## Endpoints

| Método | Caminho | Resultado |
|---|---|---|
| `POST` | `/alimentos` | Cadastra um alimento (`201`) |
| `GET` | `/alimentos` | Lista os alimentos (`200`) |
| `GET` | `/alimentos/{id}` | Busca um alimento (`200` ou `404`) |
| `PUT` | `/alimentos/{id}` | Atualiza um alimento (`200` ou `404`) |
| `DELETE` | `/alimentos/{id}` | Exclui um alimento (`204` ou `404`) |

Exemplo de cadastro:

```bash
curl -X POST http://localhost:8080/alimentos \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Arroz",
    "categoria": "Grãos",
    "quantidade": 5,
    "unidade": "kg",
    "dataValidade": "2026-10-15"
  }'
```

O `id` é gerado pelo MongoDB. A data deve usar o formato `YYYY-MM-DD`, e a quantidade deve ser maior que zero.

## Testes e cobertura

Execute os testes e valide a cobertura mínima de 70%:

```bash
mvn clean verify
```

O relatório HTML será criado em:

```text
target/site/jacoco/index.html
```

O build falha automaticamente se a cobertura de linhas ficar abaixo de 70%.
