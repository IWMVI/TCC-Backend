package br.edu.fateczl.tcc.service;

import br.edu.fateczl.tcc.enums.StatusAluguel;
import br.edu.fateczl.tcc.repository.AluguelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancasServiceTest {

    @Mock
    private AluguelRepository aluguelRepository;

    @InjectMocks
    private FinancasService financasService;

    @Test
    void obterResumo_deveCalcularTotaisDoPeriodo() {
        LocalDate inicio = YearMonth.now().atDay(1);
        LocalDate fim = YearMonth.now().atEndOfMonth();

        when(aluguelRepository.somarReceitaBrutaPorPeriodo(inicio, fim)).thenReturn(new BigDecimal("1000.00"));
        when(aluguelRepository.somarDescontosPorPeriodo(inicio, fim)).thenReturn(new BigDecimal("50.00"));
        when(aluguelRepository.somarMultasPorPeriodo(inicio, fim)).thenReturn(new BigDecimal("20.00"));
        when(aluguelRepository.countByDataAluguelBetween(inicio, fim)).thenReturn(5L);
        List<Object[]> agregados = new ArrayList<>();
        agregados.add(new Object[]{StatusAluguel.CONCLUIDO, 3L, new BigDecimal("800.00")});
        when(aluguelRepository.agregarPorStatusEPeriodo(eq(inicio), eq(fim))).thenReturn(agregados);

        var resumo = financasService.obterResumo(inicio, fim);

        assertEquals(new BigDecimal("1000.00"), resumo.receitaBruta());
        assertEquals(new BigDecimal("970.00"), resumo.receitaLiquida());
        assertEquals(5L, resumo.quantidadeAlugueis());
        assertEquals(1, resumo.porStatus().size());
    }

    @Test
    void obterResumo_deveUsarMesAtualQuandoDatasNulas() {
        when(aluguelRepository.somarReceitaBrutaPorPeriodo(any(), any())).thenReturn(BigDecimal.ZERO);
        when(aluguelRepository.somarDescontosPorPeriodo(any(), any())).thenReturn(BigDecimal.ZERO);
        when(aluguelRepository.somarMultasPorPeriodo(any(), any())).thenReturn(BigDecimal.ZERO);
        when(aluguelRepository.countByDataAluguelBetween(any(), any())).thenReturn(0L);
        when(aluguelRepository.agregarPorStatusEPeriodo(any(), any())).thenReturn(List.of());

        var resumo = financasService.obterResumo(null, null);

        assertEquals(YearMonth.now().atDay(1), resumo.dataInicio());
        assertEquals(4, resumo.porStatus().size());
    }
}
