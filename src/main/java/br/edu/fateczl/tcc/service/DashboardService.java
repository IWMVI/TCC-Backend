package br.edu.fateczl.tcc.service;

import br.edu.fateczl.tcc.domain.Aluguel;
import br.edu.fateczl.tcc.dto.aluguel.AluguelResponse;
import br.edu.fateczl.tcc.dto.dashboard.AluguelResumoItemResponse;
import br.edu.fateczl.tcc.dto.dashboard.DashboardResumoResponse;
import br.edu.fateczl.tcc.dto.dashboard.SerieMensalDashboardResponse;
import br.edu.fateczl.tcc.enums.StatusAluguel;
import br.edu.fateczl.tcc.mapper.AluguelMapper;
import br.edu.fateczl.tcc.repository.AluguelRepository;
import br.edu.fateczl.tcc.specification.AluguelSpecification;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    private final AluguelRepository aluguelRepository;

    public DashboardService(AluguelRepository aluguelRepository) {
        this.aluguelRepository = aluguelRepository;
    }

    public DashboardResumoResponse obterResumo() {
        LocalDate inicioMes = YearMonth.now().atDay(1);
        LocalDate fimMes = YearMonth.now().atEndOfMonth();

        BigDecimal receitaMes = aluguelRepository.somarValorTotalPorStatusEPeriodo(
                StatusAluguel.CONCLUIDO, inicioMes, fimMes);
        BigDecimal receitaPendente = aluguelRepository.somarValorTotalPorStatuses(
                List.of(StatusAluguel.ATIVO, StatusAluguel.ATRASO));

        List<AluguelResumoItemResponse> ultimos = aluguelRepository.findTop5ByOrderByDataAluguelDesc()
                .stream()
                .map(this::paraResumoItem)
                .toList();

        return new DashboardResumoResponse(
                aluguelRepository.countByStatus(StatusAluguel.ATIVO),
                aluguelRepository.countByStatus(StatusAluguel.ATRASO),
                aluguelRepository.countByStatus(StatusAluguel.CONCLUIDO),
                aluguelRepository.countByStatus(StatusAluguel.CANCELADO),
                receitaMes,
                receitaPendente,
                aluguelRepository.somarTotalDescontos(),
                aluguelRepository.somarTotalMultas(),
                ultimos
        );
    }

    public List<SerieMensalDashboardResponse> obterSeriesMensais(int quantidadeMeses) {
        int meses = Math.min(Math.max(quantidadeMeses, 1), 24);
        YearMonth mesAtual = YearMonth.now();
        List<SerieMensalDashboardResponse> series = new ArrayList<>(meses);

        for (int indice = meses - 1; indice >= 0; indice--) {
            YearMonth referencia = mesAtual.minusMonths(indice);
            LocalDate inicio = referencia.atDay(1);
            LocalDate fim = referencia.atEndOfMonth();

            BigDecimal receita = aluguelRepository.somarValorTotalPorStatusEPeriodo(
                    StatusAluguel.CONCLUIDO, inicio, fim);
            long quantidade = aluguelRepository.countByDataAluguelBetween(inicio, fim);
            BigDecimal multas = aluguelRepository.somarMultasPorPeriodo(inicio, fim);
            BigDecimal descontos = aluguelRepository.somarDescontosPorPeriodo(inicio, fim);

            series.add(new SerieMensalDashboardResponse(
                    referencia.toString(),
                    receita,
                    quantidade,
                    multas,
                    descontos
            ));
        }

        return series;
    }

    @Transactional(readOnly = true)
    public List<AluguelResponse> listarAlugueisPorMetrica(String tipo) {
        Specification<Aluguel> specification = specificationPorMetrica(tipo);
        return aluguelRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "dataAluguel"))
                .stream()
                .map(AluguelMapper::toResponse)
                .toList();
    }

    private Specification<Aluguel> specificationPorMetrica(String tipo) {
        LocalDate inicioMes = YearMonth.now().atDay(1);
        LocalDate fimMes = YearMonth.now().atEndOfMonth();

        return switch (tipo) {
            case "alugueis-ativos" -> Specification.where(AluguelSpecification.comStatus(StatusAluguel.ATIVO));
            case "em-atraso" -> Specification.where(AluguelSpecification.comStatus(StatusAluguel.ATRASO));
            case "receita-mes" -> Specification
                    .where(AluguelSpecification.comStatus(StatusAluguel.CONCLUIDO))
                    .and(AluguelSpecification.comDataAluguelEntre(inicioMes, fimMes));
            case "receita-pendente" -> Specification.where(
                    AluguelSpecification.comStatusIn(List.of(StatusAluguel.ATIVO, StatusAluguel.ATRASO)));
            case "multas" -> Specification.where(AluguelSpecification.comValorMultaPositivo());
            case "descontos" -> Specification.where(AluguelSpecification.comValorDescontoPositivo());
            default -> throw new IllegalArgumentException("Métrica do dashboard inválida: " + tipo);
        };
    }

    private AluguelResumoItemResponse paraResumoItem(Aluguel aluguel) {
        return new AluguelResumoItemResponse(
                aluguel.getId(),
                aluguel.getCliente().getNome(),
                aluguel.getDataAluguel(),
                aluguel.getStatus(),
                aluguel.getValorTotal()
        );
    }
}
