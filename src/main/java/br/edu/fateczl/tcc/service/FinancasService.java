package br.edu.fateczl.tcc.service;

import br.edu.fateczl.tcc.dto.financas.FinancasPorStatusResponse;
import br.edu.fateczl.tcc.dto.financas.FinancasResumoResponse;
import br.edu.fateczl.tcc.enums.StatusAluguel;
import br.edu.fateczl.tcc.repository.AluguelRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

@Service
public class FinancasService {

    private final AluguelRepository aluguelRepository;

    public FinancasService(AluguelRepository aluguelRepository) {
        this.aluguelRepository = aluguelRepository;
    }

    public FinancasResumoResponse obterResumo(LocalDate dataInicio, LocalDate dataFim) {
        LocalDate inicio = dataInicio != null ? dataInicio : YearMonth.now().atDay(1);
        LocalDate fim = dataFim != null ? dataFim : YearMonth.now().atEndOfMonth();

        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("Data fim deve ser igual ou posterior à data início");
        }

        BigDecimal receitaBruta = aluguelRepository.somarReceitaBrutaPorPeriodo(inicio, fim);
        BigDecimal descontos = aluguelRepository.somarDescontosPorPeriodo(inicio, fim);
        BigDecimal multas = aluguelRepository.somarMultasPorPeriodo(inicio, fim);
        BigDecimal receitaLiquida = receitaBruta.subtract(descontos != null ? descontos : BigDecimal.ZERO)
                .add(multas != null ? multas : BigDecimal.ZERO);

        List<FinancasPorStatusResponse> porStatus = aluguelRepository.agregarPorStatusEPeriodo(inicio, fim)
                .stream()
                .map(linha -> new FinancasPorStatusResponse(
                        (StatusAluguel) linha[0],
                        (Long) linha[1],
                        (BigDecimal) linha[2]
                ))
                .toList();

        if (porStatus.isEmpty()) {
            porStatus = Arrays.stream(StatusAluguel.values())
                    .map(s -> new FinancasPorStatusResponse(s, 0L, BigDecimal.ZERO))
                    .toList();
        }

        return new FinancasResumoResponse(
                inicio,
                fim,
                receitaBruta,
                descontos,
                multas,
                receitaLiquida,
                aluguelRepository.countByDataAluguelBetween(inicio, fim),
                porStatus
        );
    }
}
