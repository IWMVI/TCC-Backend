# Diagrama de pacotes

Backend Spring Boot — locadora de trajes a rigor (`br.edu.fateczl.tcc`).

**Regra de dependência:** `controller → service → repository → domain`. DTOs e mappers ficam na borda HTTP; entidades JPA não são expostas nos endpoints.

```mermaid
graph TB
    subgraph root["«package» br.edu.fateczl.tcc"]
        TccApplication["TccApplication"]

        subgraph pkg_controller["«package» br.edu.fateczl.tcc.controller"]
            ctrl["AluguelController<br/>ClienteController<br/>TrajeController<br/>MedidaController<br/>DevolucaoController<br/>EnumController<br/>ImagemController"]
        end

        subgraph pkg_config["«package» br.edu.fateczl.tcc.config"]
            cfg["SecurityConfig<br/>SwaggerConfig<br/>JacksonConfig<br/>EnumConverterConfig"]
        end

        subgraph pkg_service["«package» br.edu.fateczl.tcc.service"]
            svc["AluguelService<br/>ClienteService<br/>TrajeService<br/>MedidaService<br/>DevolucaoService<br/>ContratoPdfService<br/>ImagemService"]
        end

        subgraph pkg_strategy["«package» br.edu.fateczl.tcc.strategy"]
            stg["MedidaStrategy<br/>MedidaMasculinaStrategy<br/>MedidaFemininaStrategy"]
        end

        subgraph pkg_specification["«package» br.edu.fateczl.tcc.specification"]
            spec["AluguelSpecification<br/>TrajeSpecification<br/>MedidaSpecification"]
        end

        subgraph pkg_mapper["«package» br.edu.fateczl.tcc.mapper"]
            mpr["AluguelMapper<br/>ClienteMapper<br/>TrajeMapper<br/>DevolucaoMapper<br/>ItemAluguelMapper<br/>MedidaMasculinaMapper<br/>MedidaFemininaMapper"]
        end

        subgraph pkg_dto["«package» br.edu.fateczl.tcc.dto"]
            subgraph pkg_dto_aluguel["«package» br.edu.fateczl.tcc.dto.aluguel"]
                dto_alg["AluguelRequest<br/>AluguelResponse<br/>..."]
            end
            subgraph pkg_dto_devolucao["«package» br.edu.fateczl.tcc.dto.devolucao"]
                dto_dev["DevolucaoRequest<br/>DevolucaoResponse<br/>..."]
            end
            subgraph pkg_dto_traje["«package» br.edu.fateczl.tcc.dto.traje"]
                dto_trj["TrajeRequest<br/>TrajeResponse<br/>..."]
            end
            subgraph pkg_dto_masc["«package» br.edu.fateczl.tcc.dto.masculina"]
                dto_masc["MedidaMasculinaRequest<br/>..."]
            end
            subgraph pkg_dto_fem["«package» br.edu.fateczl.tcc.dto.feminina"]
                dto_fem["MedidaFemininaRequest<br/>..."]
            end
            dto_root["ClienteRequest<br/>ClienteResponse<br/>EnderecoRequest"]
        end

        subgraph pkg_repository["«package» br.edu.fateczl.tcc.repository"]
            repo["AluguelRepository<br/>ClienteRepository<br/>TrajeRepository<br/>ItemAluguelRepository<br/>DevolucaoRepository<br/>MedidaRepository<br/>MedidaMasculinaRepository<br/>MedidaFemininaRepository"]
        end

        subgraph pkg_domain["«package» br.edu.fateczl.tcc.domain"]
            subgraph pkg_domain_factory["«package» br.edu.fateczl.tcc.domain.factory"]
                fac["ClienteFactory"]
            end
            dom["Cliente<br/>Aluguel<br/>Traje<br/>ItemAluguel<br/>Devolucao<br/>Medida<br/>MedidaMasculina<br/>MedidaFeminina<br/>Endereco"]
        end

        subgraph pkg_enums["«package» br.edu.fateczl.tcc.enums"]
            enm["StatusAluguel<br/>StatusTraje<br/>CondicaoTraje<br/>SexoEnum<br/>..."]
        end

        subgraph pkg_exception["«package» br.edu.fateczl.tcc.exception"]
            exc["BusinessException<br/>ResourceNotFoundException<br/>GlobalExceptionHandler"]
        end

        subgraph pkg_util["«package» br.edu.fateczl.tcc.util"]
            utl["PortUtil"]
        end

        subgraph pkg_seeder["«package» br.edu.fateczl.tcc.seeder"]
            subgraph pkg_seeder_data["«package» br.edu.fateczl.tcc.seeder.data"]
                seed_data["ClienteSeedData<br/>TrajeSeedData<br/>AluguelSeedData<br/>DevolucaoSeedData<br/>MedidaSeedData"]
            end
            seed["DatabaseSeeder"]
        end
    end

    subgraph external["Sistemas externos"]
        React["Frontend React"]
        MySQL[(MySQL)]
    end

    React --> pkg_controller
    pkg_controller --> pkg_service
    pkg_controller --> pkg_dto
    pkg_controller --> pkg_exception
    pkg_config --> pkg_controller
    pkg_service --> pkg_repository
    pkg_service --> pkg_domain
    pkg_service --> pkg_mapper
    pkg_service --> pkg_strategy
    pkg_service --> pkg_specification
    pkg_service --> pkg_exception
    pkg_service --> pkg_enums
    pkg_mapper --> pkg_dto
    pkg_mapper --> pkg_domain
    pkg_repository --> pkg_domain
    pkg_specification --> pkg_domain
    pkg_strategy --> pkg_domain
    pkg_domain_factory --> pkg_domain
    pkg_seeder --> pkg_repository
    pkg_repository --> MySQL
```
