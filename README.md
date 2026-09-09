# EcoFood

API REST simples para cadastrar e controlar alimentos armazenados. O projeto apoia a **ODS 12 — Consumo e Produção Responsáveis**, ajudando a reduzir perdas por falta de controle de quantidade e validade.

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
  -H 'Content-Type: application/json' \
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
