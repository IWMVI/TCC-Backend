# Diagrama de sequência — devolução com cálculo de multa

Fluxo de `POST /alugueis/{id}/devolucao`.

A **multa** (`valorMulta`) é calculada no **Frontend React** e enviada no `DevolucaoRequest`. O backend valida (`@PositiveOrZero`, `@Digits`), persiste via `DevolucaoMapper` e altera o aluguel para `StatusAluguel.CONCLUIDO`.

```mermaid
sequenceDiagram
    autonumber
    actor React as Frontend React
    participant Ctrl as br.edu.fateczl.tcc.controller.AluguelController
    participant AlgSvc as br.edu.fateczl.tcc.service.AluguelService
    participant DevSvc as br.edu.fateczl.tcc.service.DevolucaoService
    participant AlgRepo as br.edu.fateczl.tcc.repository.AluguelRepository
    participant TrjRepo as br.edu.fateczl.tcc.repository.TrajeRepository
    participant DevRepo as br.edu.fateczl.tcc.repository.DevolucaoRepository
    participant DevMpr as br.edu.fateczl.tcc.mapper.DevolucaoMapper
    participant DB as MySQL

    Note over React: Calcula multa no cliente<br/>(ex.: dias de atraso × valor/dia)

    React->>Ctrl: POST /alugueis/{id}/devolucao<br/>(DevolucaoRequest)
    Ctrl->>Ctrl: @Valid (valorMulta, itens, datas)
    Ctrl->>AlgSvc: registrarDevolucao(id, dto)

    activate AlgSvc
    Note over AlgSvc: @Transactional

    AlgSvc->>AlgRepo: findById(aluguelId)
    AlgRepo->>DB: SELECT aluguel
    DB-->>AlgRepo: Aluguel ATIVO
    AlgRepo-->>AlgSvc: Aluguel

    alt aluguel não ATIVO ou não encontrado
        AlgSvc-->>Ctrl: BusinessException / NotFound
        Ctrl-->>React: 400 / 404
    end

    loop para cada ItemDevolucaoRequest
        AlgSvc->>TrjRepo: findById(trajeId)
        TrjRepo->>DB: SELECT traje
        DB-->>TrjRepo: Traje
        AlgSvc->>AlgSvc: traje.condicao = item.condicao<br/>traje.status = DISPONIVEL
        AlgSvc->>TrjRepo: save(traje)
        TrjRepo->>DB: UPDATE traje
    end

    AlgSvc->>DevSvc: criar(dto, aluguel)

    activate DevSvc
    DevSvc->>DevRepo: existsByAluguelId(aluguelId)
    DevRepo->>DB: SELECT EXISTS devolucao
    DB-->>DevRepo: false

    DevSvc->>DevMpr: toEntity(dto, aluguel)
    Note over DevMpr: valorMulta do DTO<br/>(sem recálculo no servidor)
    DevMpr-->>DevSvc: Devolucao

    DevSvc->>DevRepo: save(devolucao)
    DevRepo->>DB: INSERT devolucao
    DB-->>DevRepo: Devolucao

    DevSvc->>DevMpr: toResponse(devolucao)
    DevMpr-->>DevSvc: DevolucaoResponse
    deactivate DevSvc

    DevSvc-->>AlgSvc: DevolucaoResponse

    AlgSvc->>AlgSvc: aluguel.status = CONCLUIDO
    AlgSvc->>AlgRepo: save(aluguel)
    AlgRepo->>DB: UPDATE aluguel
    deactivate AlgSvc

    AlgSvc-->>Ctrl: DevolucaoResponse
    Ctrl-->>React: 201 Created
```
