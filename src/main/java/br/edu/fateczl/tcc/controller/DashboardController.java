package br.edu.fateczl.tcc.controller;

import br.edu.fateczl.tcc.dto.aluguel.AluguelResponse;
import br.edu.fateczl.tcc.dto.dashboard.DashboardResumoResponse;
import br.edu.fateczl.tcc.dto.dashboard.SerieMensalDashboardResponse;
import br.edu.fateczl.tcc.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard Controller", description = "Métricas do painel")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Resumo do dashboard")
    @GetMapping("/resumo")
    public DashboardResumoResponse resumo() {
        return dashboardService.obterResumo();
    }

    @Operation(summary = "Séries mensais para gráficos do dashboard")
    @GetMapping("/series-mensais")
    public List<SerieMensalDashboardResponse> seriesMensais(
            @RequestParam(defaultValue = "12") int meses) {
        return dashboardService.obterSeriesMensais(meses);
    }

    @Operation(summary = "Aluguéis vinculados a uma métrica do dashboard")
    @GetMapping("/metricas/{tipo}/alugueis")
    public List<AluguelResponse> alugueisPorMetrica(@PathVariable String tipo) {
        return dashboardService.listarAlugueisPorMetrica(tipo);
    }
}
