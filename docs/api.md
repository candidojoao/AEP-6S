# API

Endereço padrão: `http://localhost:8080`. Todos os endpoints podem ser testados pelo navegador no Swagger UI: **http://localhost:8080/swagger-ui.html** (botão **Try it out** em cada operação).

Datas usam o formato `AAAA-MM-DD`. Quantidades aceitam casas decimais (`0.5`).

## Residências

### Cadastrar

`POST /residencias`

```json
{
  "nome": "Casa da família Silva",
  "responsavel": "Ana Silva",
  "endereco": {
    "rua": "Rua das Palmeiras",
    "numero": "120",
    "bairro": "Zona 7",
    "cidade": "Maringá",
    "estado": "PR",
    "cep": "87020-000"
  },
  "locais": [
    { "nome": "Geladeira", "tipo": "REFRIGERADO" },
    { "nome": "Despensa", "tipo": "AMBIENTE" }
  ]
}
```

Resposta `201` com o documento e o `id` gerado. O `tipo` do local aceita `AMBIENTE`, `REFRIGERADO` ou `CONGELADO`.

### Adicionar um local

`POST /residencias/{id}/locais`

```json
{ "nome": "Freezer", "tipo": "CONGELADO" }
```

Resposta `201` com a residência atualizada. Se a casa já tiver um local com esse nome (sem diferenciar maiúsculas), a resposta é `422`.

### Demais operações

| Operação | Resposta |
|---|---|
| `GET /residencias` | lista |
| `GET /residencias/{id}` | `200` ou `404` |
| `PUT /residencias/{id}` (mesmo corpo do cadastro) | `200`, `404`, ou `422` se remover um local que ainda guarda alimentos |
| `DELETE /residencias/{id}` | `204`, `404`, ou `422` se ainda houver alimentos na residência |

## Categorias

`POST /categorias`

```json
{
  "nome": "Laticínios",
  "diasAlertaValidade": 3,
  "dicasConservacao": ["Manter refrigerado entre 1 °C e 5 °C"]
}
```

Resposta `201`. `diasAlertaValidade` vai de 0 a 365. Um nome repetido responde `422`.

| Operação | Resposta |
|---|---|
| `GET /categorias` | lista |
| `GET /categorias/{id}` | `200` ou `404` |
| `PUT /categorias/{id}` | `200`, `404` ou `422` |
| `DELETE /categorias/{id}` | `204`, `404`, ou `422` se a categoria estiver em uso |

## Alimentos

### Cadastrar

`POST /alimentos`

```json
{
  "nome": "Leite integral",
  "residenciaId": "6af3761d47b97c882d4738f4",
  "categoriaId": "6af3761d47b97c882d4738ef",
  "local": "Geladeira",
  "quantidade": 4,
  "unidade": "L",
  "dataValidade": "2026-11-12"
}
```

| Situação | Resposta |
|---|---|
| tudo certo | `201` com o alimento e `movimentacoes` vazia |
| residência ou categoria inexistente | `404` |
| o local não existe na residência | `422` |
| campo faltando ou inválido | `400` |

O local é gravado com o nome exato da residência: `"geladeira"` vira `"Geladeira"`.

### Listar

- `GET /alimentos` lista todos.
- `GET /alimentos?residenciaId={id}` lista só os da residência, do que vence primeiro para o último.

### Atualizar e excluir

| Operação | Resposta |
|---|---|
| `GET /alimentos/{id}` | `200` ou `404` |
| `PUT /alimentos/{id}` (mesmo corpo do cadastro) | `200`; o histórico de movimentações é mantido |
| `DELETE /alimentos/{id}` | `204` ou `404` |

## Movimentações

### Registrar

`POST /alimentos/{id}/movimentacoes`

```json
{ "tipo": "CONSUMO", "quantidade": 0.5, "observacao": "Café da manhã" }
```

| Campo | Obrigatório | Observação |
|---|---|---|
| `tipo` | sim | `CONSUMO`, `DESCARTE`, `DOACAO` (tiram do estoque) ou `REPOSICAO` (soma) |
| `quantidade` | sim | maior que zero, na mesma unidade do alimento |
| `data` | não | se não for informada, vale a data de hoje; não pode estar no futuro |
| `observacao` | não | até 200 caracteres |

Resposta `201` com o alimento atualizado:

```json
{
  "id": "6af3761d47b97c882d4738f6",
  "nome": "Leite integral",
  "residenciaId": "6af3761d47b97c882d4738f4",
  "categoriaId": "6af3761d47b97c882d4738ef",
  "local": "Geladeira",
  "quantidade": 3.5,
  "unidade": "L",
  "dataValidade": "2026-11-12",
  "movimentacoes": [
    { "tipo": "CONSUMO", "quantidade": 0.5, "data": "2026-11-10", "observacao": "Café da manhã" }
  ]
}
```

Uma saída maior que o estoque responde `422`:

```json
{
  "timestamp": "2026-11-10T19:27:45.229Z",
  "status": 422,
  "mensagem": "Quantidade maior que o estoque disponível: 3.5 L",
  "campos": {}
}
```

### Histórico

`GET /alimentos/{id}/movimentacoes` devolve a lista de movimentações do alimento.

## Relatórios

### Alertas de validade

`GET /residencias/{id}/alertas-validade`

Lista os alimentos com estoque cujo vencimento está a até `diasAlertaValidade` dias, conforme a categoria de cada um, ordenados pela validade. Início da resposta com os dados de exemplo (6 alertas no total):

```json
[
  { "alimentoId": "6af3761d47b97c882d4738f7", "alimento": "Iogurte natural", "categoria": "Laticínios",
    "local": "Geladeira", "quantidade": 3, "unidade": "un", "dataValidade": "2026-11-09",
    "diasParaVencer": -1, "situacao": "VENCIDO" },
  { "alimentoId": "6af3761d47b97c882d4738f9", "alimento": "Peito de frango", "categoria": "Carnes",
    "local": "Freezer", "quantidade": 1, "unidade": "kg", "dataValidade": "2026-11-10",
    "diasParaVencer": 0, "situacao": "VENCE_HOJE" },
  { "alimentoId": "6af3761d47b97c882d4738fc", "alimento": "Banana", "categoria": "Hortifrúti",
    "local": "Despensa", "quantidade": 8, "unidade": "un", "dataValidade": "2026-11-11",
    "diasParaVencer": 1, "situacao": "PERTO_DO_VENCIMENTO" }
]
```

`situacao` vale `VENCIDO` (dias negativos), `VENCE_HOJE` (0) ou `PERTO_DO_VENCIMENTO` (1 em diante). Alimentos sem estoque não geram alerta.

### Relatório de desperdício

`GET /residencias/{id}/relatorio-desperdicio`

```json
{
  "residenciaId": "6af3761d47b97c882d4738f4",
  "residencia": "Casa da família Silva",
  "porUnidade": [
    { "unidade": "L", "consumido": 2, "doado": 0, "descartado": 0, "aproveitamentoPercentual": 100.0 },
    { "unidade": "kg", "consumido": 2.4, "doado": 1, "descartado": 0.2, "aproveitamentoPercentual": 94.4 },
    { "unidade": "un", "consumido": 8, "doado": 3, "descartado": 4, "aproveitamentoPercentual": 73.3 }
  ],
  "maisDescartados": [
    { "nome": "Banana", "unidade": "un", "quantidade": 2 },
    { "nome": "Alface", "unidade": "un", "quantidade": 1 },
    { "nome": "Pão de forma", "unidade": "un", "quantidade": 1 },
    { "nome": "Tomate", "unidade": "kg", "quantidade": 0.2 }
  ]
}
```

- Os totais são separados por unidade, porque não faz sentido somar quilos com litros.
- `aproveitamentoPercentual` = (consumido + doado) ÷ (consumido + doado + descartado) × 100.
- `maisDescartados` traz até 5 alimentos.
- Os dois relatórios respondem `404` se a residência não existir.

## Erros

| Status | Quando | Exemplo de `mensagem` |
|---|---|---|
| 400 | campo inválido | `Dados inválidos`, com `campos`: `{ "endereco.cidade": "A cidade é obrigatória" }` |
| 400 | JSON malformado, data fora do formato ou tipo desconhecido | `Corpo da requisição inválido` |
| 404 | registro inexistente | `Residência não encontrada: 6af3...` |
| 409 | o índice único barrou um nome repetido | `Já existe um registro com esses dados` |
| 422 | regra de negócio | `A residência Casa da família Silva não possui o local: Varanda` |
