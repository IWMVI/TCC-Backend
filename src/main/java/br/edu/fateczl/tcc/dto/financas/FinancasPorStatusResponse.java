package br.edu.fateczl.tcc.dto.financas;

import br.edu.fateczl.tcc.enums.StatusAluguel;

import java.math.BigDecimal;

public record FinancasPorStatusResponse(
        StatusAluguel status,
        long quantidade,
        BigDecimal valorTotal
) {
}
