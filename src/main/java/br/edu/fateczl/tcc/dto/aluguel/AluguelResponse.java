package br.edu.fateczl.tcc.dto.aluguel;

import br.edu.fateczl.tcc.enums.StatusAluguel;
import br.edu.fateczl.tcc.enums.TipoOcasiao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AluguelResponse(

        Long id,
        Long clienteId,
        String nomeCliente,

        LocalDate dataAluguel,
        LocalDate dataRetirada,
        LocalDate dataDevolucao,

        BigDecimal valorTotal,
        BigDecimal valorDesconto,
        BigDecimal valorMulta,

        String observacoes,
        StatusAluguel status,
        TipoOcasiao ocasiao,

        List<ItemAluguelResponse> itens

) { }
