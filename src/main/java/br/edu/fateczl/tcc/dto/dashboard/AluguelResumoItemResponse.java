package br.edu.fateczl.tcc.dto.dashboard;

import br.edu.fateczl.tcc.enums.StatusAluguel;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AluguelResumoItemResponse(
        Long id,
        String nomeCliente,
        LocalDate dataAluguel,
        StatusAluguel status,
        BigDecimal valorTotal
) {
}
