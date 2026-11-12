# Changelog

## 2.0.0 — 2ª entrega

### Banco de dados

- Duas coleções novas: `residencias` e `categorias`.
- `residencias` com subdocumento (`endereco`) e lista de subdocumentos (`locais`).
- `alimentos` passa a referenciar uma residência e uma categoria por `ObjectId` (`residenciaId`, `categoriaId`) e a guardar a lista de subdocumentos `movimentacoes`.
- Índices: nome único em `categorias`, `{ residenciaId, dataValidade }` e `{ categoriaId }` em `alimentos`.
- Agregações para o relatório de desperdício (`$match`, `$unwind`, `$group`, `$sort`, `$limit`).
- Documentos sem o campo técnico `_class`.
- Script de dados de exemplo (`scripts/dados-exemplo.js`).

### Funcionalidades

- CRUD de residências, com inclusão de novos locais de armazenamento.
- CRUD de categorias, com dias de alerta de validade e dicas de conservação.
- Alimentos ligados a residência, categoria e local; listagem por residência em ordem de validade.
- Movimentações de estoque: consumo, descarte, doação e reposição.
- Alertas de validade por residência.
- Relatório de desperdício por residência, com percentual de aproveitamento e alimentos mais descartados.
- Proteção das ligações: não excluir residência ou categoria em uso, nem remover um local que guarda alimentos.

### Código

- Classe base `ServicoCrud<T>` com as operações comuns dos serviços.
- Hierarquia de exceções (`RecursoNaoEncontradoException` e `RegraDeNegocioException`), com respostas 404, 409 e 422 padronizadas.
- Regras de negócio dentro das entidades (`Alimento`, `Residencia`, `Categoria`) e nos enums (`TipoMovimentacao`, `SituacaoValidade`).
- Conversão dos DTOs para o domínio num único método, sem repetição entre controller e serviço.
- `Clock` injetado para as regras de data.
- Swagger UI (springdoc-openapi).
- Maven Wrapper.
- Limite de cabeçalho HTTP de 64 KB, para o navegador conseguir abrir o Swagger mesmo com muitos cookies de `localhost`.

### Testes

- De 18 para 133 testes, com cobertura de 99,5% das linhas.
- Testes de integração com MongoDB embutido no perfil Maven `integracao`.

### Mudanças que quebram a compatibilidade

- `POST` e `PUT /alimentos` agora exigem `residenciaId`, `categoriaId` e `local`; o campo de texto `categoria` deixou de existir.
- Os alimentos gravados pela versão 1.0 continuam sendo lidos, mas não aparecem nos alertas nem nos relatórios, porque não pertencem a nenhuma residência.

## 1.0 — 1ª entrega (commit `84b680b`, 09/09/2026)

- API REST de alimentos com CRUD completo na coleção `alimentos`.
- Validação dos campos com Bean Validation e respostas de erro padronizadas (400 e 404).
- Quantidade gravada como `Decimal128`.
- 18 testes automatizados, com 97,5% de cobertura de linhas.
