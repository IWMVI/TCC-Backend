package br.edu.fateczl.tcc.mapper;

import br.edu.fateczl.tcc.domain.Aluguel;
import br.edu.fateczl.tcc.domain.Cliente;
import br.edu.fateczl.tcc.dto.aluguel.AluguelRequest;
import br.edu.fateczl.tcc.dto.aluguel.AluguelResponse;
import br.edu.fateczl.tcc.dto.aluguel.AluguelUpdateRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AluguelMapper {

    private AluguelMapper() {}

    public static Aluguel toEntity(AluguelRequest dto, Cliente cliente) {
        return Aluguel.builder()
                .cliente(cliente)
                .dataAluguel(LocalDate.now())
                .dataRetirada(dto.dataRetirada())
                .dataDevolucao(dto.dataDevolucao())
                .valorDesconto(dto.valorDesconto())
                .observacoes(dto.observacoes())
                .ocasiao(dto.ocasiao())
                .build();
    }

    public static void updateEntity(Aluguel entity, AluguelUpdateRequest dto) {
        entity.setDataRetirada(dto.dataRetirada());
        entity.setDataDevolucao(dto.dataDevolucao());
        entity.setValorDesconto(dto.valorDesconto());
        entity.setObservacoes(dto.observacoes());
        entity.setStatus(dto.status());
        entity.setOcasiao(dto.ocasiao());
    }

    private static BigDecimal valorMultaOuZero(Aluguel entity) {
        return entity.getValorMulta() != null ? entity.getValorMulta() : BigDecimal.ZERO;
    }

    public static AluguelResponse toResponse(Aluguel entity) {
        return new AluguelResponse(
                entity.getId(),
                entity.getCliente().getId(),
                entity.getCliente().getNome(),
                entity.getDataAluguel(),
                entity.getDataRetirada(),
                entity.getDataDevolucao(),
                entity.getValorTotal(),
                entity.getValorDesconto(),
                valorMultaOuZero(entity),
                entity.getObservacoes(),
                entity.getStatus(),
                entity.getOcasiao(),
                entity.getItens()
                        .stream()
                        .map(ItemAluguelMapper::toResponse)
                        .toList()
        );
    }
}
