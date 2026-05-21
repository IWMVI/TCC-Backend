# Diagrama entidade-relacionamento (ER)

Modelo lógico do banco MySQL, conforme mapeamento JPA (`ddl-auto: update`).

- **Endereço:** colunas embutidas na tabela `cliente` (`@Embedded` em `Cliente`).
- **Medida:** herança `JOINED` — tabela base `medida` + `medida_masculina` / `medida_feminina`.

```mermaid
erDiagram
    CLIENTE ||--o{ ALUGUEL : realiza
    CLIENTE ||--o{ MEDIDA : possui
    CLIENTE {
        bigint id PK
        varchar nome
        varchar cpf_cnpj
        varchar email
        varchar celular
        varchar sexo
        date data_cadastro
        boolean ativo
        varchar cep
        varchar logradouro
        varchar numero
        varchar cidade
        varchar bairro
        varchar estado
        varchar complemento
    }

    ALUGUEL ||--|{ ITEM_ALUGUEL : contem
    ALUGUEL ||--o| DEVOLUCAO : encerra
    ALUGUEL {
        bigint id PK
        date data_aluguel
        date data_retirada
        date data_devolucao
        decimal valor_total
        decimal valor_desconto
        varchar observacoes
        varchar status
        varchar ocasiao
        bigint id_cliente FK
    }

    TRAJE ||--o{ ITEM_ALUGUEL : referenciado
    TRAJE {
        bigint id PK
        varchar nome
        varchar descricao
        decimal valor_item
        varchar status
        varchar condicao
        varchar tamanho
        varchar cor
        varchar tipo
        varchar genero
        varchar tecido
        varchar estampa
        varchar textura
        longtext imagem_url
        datetime data_cadastro
    }

    ITEM_ALUGUEL {
        bigint id PK
        bigint id_aluguel FK
        bigint id_traje FK
    }

    DEVOLUCAO {
        bigint id PK
        date data_devolucao
        decimal valor_multa
        varchar observacoes
        bigint id_aluguel FK
    }

    MEDIDA ||--o| MEDIDA_MASCULINA : especializa
    MEDIDA ||--o| MEDIDA_FEMININA : especializa
    MEDIDA {
        bigint id PK
        decimal cintura
        decimal manga
        varchar sexo
        date data_medida
        bigint id_cliente FK
    }

    MEDIDA_MASCULINA {
        bigint id PK, FK
        decimal colarinho
        decimal barra
        decimal torax
    }

    MEDIDA_FEMININA {
        bigint id PK, FK
        decimal altura_busto
        decimal raio_busto
        decimal corpo
        decimal ombro
        decimal decote
        decimal quadril
        decimal comprimento_vestido
    }
```
