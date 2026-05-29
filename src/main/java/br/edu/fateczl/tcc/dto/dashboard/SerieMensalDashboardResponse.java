package br.edu.fateczl.tcc.dto.dashboard;

import java.math.BigDecimal;

public record SerieMensalDashboardResponse(
        String mes,
        BigDecimal receita,
        long quantidadeAlugueis,
        BigDecimal multas,
        BigDecimal descontos
) {
}
