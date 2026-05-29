package br.edu.fateczl.tcc.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResumoResponse(
        long alugueisAtivos,
        long alugueisEmAtraso,
        long alugueisConcluidos,
        long alugueisCancelados,
        BigDecimal receitaMesAtual,
        BigDecimal receitaPendente,
        BigDecimal totalDescontos,
        BigDecimal totalMultas,
        List<AluguelResumoItemResponse> ultimosAlugueis
) {
}
