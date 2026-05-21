# Diagrama de sequência — criação de aluguel

Fluxo feliz de `POST /alugueis`, do Frontend React até a persistência em MySQL.

| Etapa | Responsável |
|-------|-------------|
| Validação HTTP | `br.edu.fateczl.tcc.controller.AluguelController` |
| Regras de negócio | `br.edu.fateczl.tcc.service.AluguelService` |
| Conflito de período | `br.edu.fateczl.tcc.repository.ItemAluguelRepository.trajeIndisponivelNoPeriodo` |
| Status inicial | `StatusAluguel.ATIVO` |

```mermaid
sequenceDiagram
    autonumber
    actor React as Frontend React
    participant Ctrl as br.edu.fateczl.tcc.controller.AluguelController
    participant Svc as br.edu.fateczl.tcc.service.AluguelService
    participant CliRepo as br.edu.fateczl.tcc.repository.ClienteRepository
    participant TrjRepo as br.edu.fateczl.tcc.repository.TrajeRepository
    participant ItemRepo as br.edu.fateczl.tcc.repository.ItemAluguelRepository
    participant AlgRepo as br.edu.fateczl.tcc.repository.AluguelRepository
    participant Mpr as br.edu.fateczl.tcc.mapper.AluguelMapper
    participant DB as MySQL

    React->>Ctrl: POST /alugueis (AluguelRequest)
    Ctrl->>Ctrl: @Valid Bean Validation
    Ctrl->>Svc: criar(dto)

    activate Svc
    Note over Svc: @Transactional

    Svc->>CliRepo: findById(clienteId)
    CliRepo->>DB: SELECT cliente
    DB-->>CliRepo: Cliente
    CliRepo-->>Svc: Optional Cliente

    alt cliente não encontrado
        Svc-->>Ctrl: ResourceNotFoundException
        Ctrl-->>React: 404 ProblemDetail
    end

    Svc->>Svc: validarDatas(retirada, devolucao)

    Svc->>Mpr: toEntity(dto, cliente)
    Mpr-->>Svc: Aluguel
    Svc->>Svc: status = ATIVO

    loop para cada ItemAluguelRequest
        Svc->>TrjRepo: findById(trajeId)
        TrjRepo->>DB: SELECT traje
        DB-->>TrjRepo: Traje
        TrjRepo-->>Svc: Traje

        Svc->>Svc: validarTrajeDisponivel(status=DISPONIVEL)

        Svc->>ItemRepo: trajeIndisponivelNoPeriodo(trajeId, retirada, devolucao, null)
        ItemRepo->>DB: SELECT COUNT conflitos ATIVOS
        DB-->>ItemRepo: boolean
        ItemRepo-->>Svc: indisponivel

        alt traje indisponível no período
            Svc-->>Ctrl: BusinessException
            Ctrl-->>React: 400 ProblemDetail
        end

        Svc->>Svc: ItemAluguelMapper.toEntity(traje, aluguel)
    end

    Svc->>Svc: calcularValorTotal(itens) - desconto
    Svc->>Svc: validarValorComDesconto >= 0

    Svc->>AlgRepo: save(aluguel + itens cascade)
    AlgRepo->>DB: INSERT aluguel, item_aluguel
    DB-->>AlgRepo: Aluguel persistido

    Svc->>Mpr: toResponse(aluguel)
    Mpr-->>Svc: AluguelResponse
    deactivate Svc

    Svc-->>Ctrl: AluguelResponse
    Ctrl-->>React: 201 Created
```
