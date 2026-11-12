# Arquitetura

O EcoFood é uma API REST em Java 21 com Spring Boot 3.5, organizada em camadas, com as regras de negócio dentro das próprias entidades do domínio.

## Camadas

```mermaid
flowchart TB
    subgraph Web["Camada web"]
        C["Controllers<br/>ResidenciaController, CategoriaController,<br/>AlimentoController, RelatorioController"]
        H["ApiExceptionHandler"]
        D["DTOs de entrada e saída<br/>(records com Bean Validation)"]
    end
    subgraph Aplicacao["Camada de serviço"]
        S["ServicoCrud (genérico) e serviços concretos<br/>AlertaValidadeService, RelatorioDesperdicioService"]
    end
    subgraph Dominio["Domínio"]
        M["Residencia, Categoria, Alimento<br/>Endereco, LocalArmazenamento, Movimentacao<br/>TipoLocal, TipoMovimentacao, SituacaoValidade"]
    end
    subgraph Dados["Persistência"]
        R["Repositórios Spring Data<br/>RelatorioDesperdicioRepository (agregações)"]
    end
    C --> D
    C --> S
    S --> M
    S --> R
    R --> DB[("MongoDB")]
```

Uma requisição percorre sempre o mesmo caminho:

1. o controller recebe o JSON e o Spring valida o DTO (`@Valid`);
2. o serviço confere as referências entre coleções e chama o domínio;
3. a entidade aplica a regra de negócio e muda o próprio estado;
4. o repositório grava o documento no MongoDB;
5. qualquer exceção vira uma resposta padronizada no `ApiExceptionHandler`.

## Pacotes

| Pacote | Responsabilidade |
|---|---|
| `config` | Conversão de `BigDecimal` para `Decimal128`, conversor sem o campo `_class`, relógio do sistema (`Clock`) e descrição do Swagger |
| `controller` | Endpoints REST, sem regra de negócio |
| `dto` | Dados de entrada (`*Request`) com a conversão para o domínio, e respostas dos relatórios |
| `model` | Entidades, objetos de valor e enums, com as regras de negócio |
| `service` | Casos de uso e verificação das referências entre coleções |
| `repository` | Acesso ao MongoDB: consultas derivadas e pipelines de agregação |
| `exception` | Hierarquia de erros e tradução para respostas HTTP |

## Diagrama de classes do domínio

```mermaid
classDiagram
    class Residencia {
        -String id
        -String nome
        -String responsavel
        -Endereco endereco
        -List~LocalArmazenamento~ locais
        +possuiLocal(String nome) boolean
        +buscarLocal(String nome) Optional~LocalArmazenamento~
        +adicionarLocal(LocalArmazenamento local)
    }
    class Endereco {
        <<record>>
        rua
        numero
        bairro
        cidade
        estado
        cep
    }
    class LocalArmazenamento {
        <<record>>
        nome
        TipoLocal tipo
        +temNome(String outro) boolean
    }
    class TipoLocal {
        <<enum>>
        AMBIENTE
        REFRIGERADO
        CONGELADO
    }
    class Categoria {
        -String id
        -String nome
        -int diasAlertaValidade
        -List~String~ dicasConservacao
        +deveAlertar(long diasParaVencer) boolean
    }
    class Alimento {
        -String id
        -String nome
        -String residenciaId
        -String categoriaId
        -String local
        -BigDecimal quantidade
        -String unidade
        -LocalDate dataValidade
        -List~Movimentacao~ movimentacoes
        +registrarMovimentacao(Movimentacao m)
        +atualizarCadastro(Alimento dados)
        +diasParaVencer(LocalDate hoje) long
        +temEstoque() boolean
    }
    class Movimentacao {
        <<record>>
        TipoMovimentacao tipo
        BigDecimal quantidade
        LocalDate data
        String observacao
    }
    class TipoMovimentacao {
        <<enum>>
        CONSUMO
        DESCARTE
        DOACAO
        REPOSICAO
        +isSaida() boolean
        +aplicarEm(BigDecimal estoque, BigDecimal quantidade) BigDecimal
    }
    class SituacaoValidade {
        <<enum>>
        VENCIDO
        VENCE_HOJE
        PERTO_DO_VENCIMENTO
        +para(long dias)$ SituacaoValidade
    }

    Residencia *-- Endereco : embutido
    Residencia *-- "1..*" LocalArmazenamento : embutidos
    LocalArmazenamento --> TipoLocal
    Alimento *-- "0..*" Movimentacao : embutidas
    Movimentacao --> TipoMovimentacao
    Alimento ..> Residencia : residenciaId
    Alimento ..> Categoria : categoriaId
```

As setas cheias com losango são subdocumentos gravados dentro do documento pai. As setas tracejadas são referências por `ObjectId` para outra coleção.

## Serviços e erros

```mermaid
classDiagram
    class ServicoCrud~T~ {
        <<abstract>>
        +listar() List~T~
        +buscarPorId(String id) T
        +excluir(String id)
        #garantirQueExiste(String id)
        #antesDeExcluir(String id)
        #naoEncontrado(String id)* RecursoNaoEncontradoException
    }
    ServicoCrud <|-- ResidenciaService
    ServicoCrud <|-- CategoriaService
    ServicoCrud <|-- AlimentoService
    AlimentoService --> ResidenciaService : confere residência e local
    AlimentoService --> CategoriaService : confere categoria
    AlertaValidadeService --> ResidenciaService
    RelatorioDesperdicioService --> ResidenciaService
    RelatorioDesperdicioService --> RelatorioDesperdicioRepository

    class RecursoNaoEncontradoException {
        <<abstract>>
    }
    RecursoNaoEncontradoException <|-- ResidenciaNaoEncontradaException
    RecursoNaoEncontradoException <|-- CategoriaNaoEncontradaException
    RecursoNaoEncontradoException <|-- AlimentoNaoEncontradoException
    RuntimeException <|-- RecursoNaoEncontradoException
    RuntimeException <|-- RegraDeNegocioException
```

## Orientação a objetos no projeto

| Princípio | Onde aparece |
|---|---|
| **Encapsulamento** | As listas `locais` e `movimentacoes` só podem ser lidas de fora; para mudar, é preciso passar por `adicionarLocal` ou `registrarMovimentacao`, que aplicam as regras. O estoque do `Alimento` só muda por movimentação. |
| **Herança** | `ResidenciaService`, `CategoriaService` e `AlimentoService` herdam de `ServicoCrud<T>` listar, buscar e excluir, sem repetir esse código. As exceções de "não encontrado" herdam de `RecursoNaoEncontradoException`. |
| **Polimorfismo** | Cada serviço implementa `naoEncontrado()` com o seu próprio erro, e `antesDeExcluir()` com a sua trava. O `ApiExceptionHandler` trata `RecursoNaoEncontradoException` uma única vez para as três subclasses. `TipoMovimentacao.aplicarEm()` soma ou subtrai do estoque conforme o tipo, sem `if` no `Alimento`. |
| **Abstração** | `ServicoCrud<T>` define o esqueleto das operações (método modelo) e deixa só os pontos variáveis para as subclasses. |
| **Objetos de valor** | `Endereco`, `LocalArmazenamento` e `Movimentacao` são `record`s imutáveis, sem identidade própria, gravados dentro do documento pai. |
| **Responsabilidade única** | O controller só traduz HTTP; o serviço coordena; a entidade decide a regra; o repositório só acessa o banco. Cada DTO tem um único método de conversão para o domínio (`paraAlimento`, `paraResidencia`, `paraCategoria`, `paraMovimentacao`). |
| **Injeção de dependência** | As dependências chegam pelo construtor, inclusive o `Clock`, o que permite testar regras de data com um relógio fixo. |

## Regras de negócio

| Regra | Onde fica | Resposta |
|---|---|---|
| A residência não pode ter dois locais com o mesmo nome | `Residencia` | 422 |
| O alimento precisa de uma residência e uma categoria existentes | `AlimentoService` | 404 |
| O local do alimento precisa existir na residência (sem diferenciar maiúsculas) | `AlimentoService` + `Residencia.buscarLocal` | 422 |
| Saída (consumo, descarte, doação) não pode passar do estoque | `Alimento.registrarMovimentacao` | 422 |
| Não excluir residência ou categoria com alimentos ligados | `antesDeExcluir` dos serviços | 422 |
| Não remover da residência um local que ainda guarda alimentos | `ResidenciaService.atualizar` | 422 |
| Nome de categoria é único | `CategoriaService` e índice único no MongoDB | 422 ou 409 |
| Alerta quando faltam até `diasAlertaValidade` dias para vencer | `Categoria.deveAlertar` | — |
| Atualizar um alimento não apaga o histórico de movimentações | `Alimento.atualizarCadastro` | — |

## Tratamento de erros

Todas as respostas de erro têm o mesmo formato:

```json
{
  "timestamp": "2026-11-10T19:27:45.229Z",
  "status": 422,
  "mensagem": "Quantidade maior que o estoque disponível: 1.5 L",
  "campos": {}
}
```

| Status | Quando |
|---|---|
| 400 | Campo inválido (`campos` lista cada campo e o motivo, inclusive dentro de subdocumentos, como `endereco.cidade` e `locais[0].nome`) ou JSON malformado |
| 404 | Residência, categoria ou alimento inexistente |
| 409 | O índice único do MongoDB barrou um registro repetido |
| 422 | Dados válidos que violam uma regra de negócio |

## Exemplo de fluxo: registrar uma movimentação

```mermaid
sequenceDiagram
    participant Cliente
    participant AlimentoController
    participant AlimentoService
    participant Alimento
    participant AlimentoRepository
    Cliente->>AlimentoController: POST /alimentos/{id}/movimentacoes
    AlimentoController->>AlimentoService: registrarMovimentacao(id, request)
    AlimentoService->>AlimentoRepository: findById(id)
    AlimentoService->>Alimento: registrarMovimentacao(movimentação com a data de hoje)
    Alimento->>Alimento: confere o estoque e aplica o tipo
    AlimentoService->>AlimentoRepository: save(alimento)
    AlimentoController-->>Cliente: 201 com o alimento e o histórico
```
