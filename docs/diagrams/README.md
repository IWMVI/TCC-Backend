# Diagramas UML (Mermaid)

Diagramas do backend TCC — locadora de trajes a rigor.

| Arquivo | Conteúdo |
|---------|----------|
| [diagrama-pacotes.md](./diagrama-pacotes.md) | Estrutura de pacotes Java e dependências |
| [diagrama-classes.md](./diagrama-classes.md) | Domínio JPA e serviços de aluguel/devolução |
| [diagrama-sequencia-criacao-aluguel.md](./diagrama-sequencia-criacao-aluguel.md) | `POST /alugueis` |
| [diagrama-sequencia-devolucao-multa.md](./diagrama-sequencia-devolucao-multa.md) | `POST /alugueis/{id}/devolucao` |
| [diagrama-er.md](./diagrama-er.md) | Modelo entidade-relacionamento (MySQL) |

## Visualização

- GitHub e GitLab renderizam blocos ` ```mermaid ` nativamente.
- [mermaid.live](https://mermaid.live) — cole o conteúdo do bloco para editar ou exportar PNG/SVG.
- VS Code / Cursor — extensão **Markdown Preview Mermaid Support**.

## Convenções usadas

- Pacotes no padrão Java: `br.edu.fateczl.tcc.<módulo>`.
- Cliente HTTP: **Frontend React**.
- Documentação de arquitetura geral: [../arch/ARCHITECTURE.md](../arch/ARCHITECTURE.md).
