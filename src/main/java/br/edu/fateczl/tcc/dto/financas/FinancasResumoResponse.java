package br.edu.fateczl.tcc.dto.financas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FinancasResumoResponse(
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal receitaBruta,
        BigDecimal totalDescontos,
        BigDecimal totalMultas,
        BigDecimal receitaLiquida,
        long quantidadeAlugueis,
        List<FinancasPorStatusResponse> porStatus
) {
}
