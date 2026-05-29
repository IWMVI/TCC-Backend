package br.edu.fateczl.tcc.controller;

import br.edu.fateczl.tcc.dto.financas.FinancasResumoResponse;
import br.edu.fateczl.tcc.service.FinancasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/financas")
@Tag(name = "Financas Controller", description = "Relatórios financeiros agregados")
public class FinancasController {

    private final FinancasService financasService;

    public FinancasController(FinancasService financasService) {
        this.financasService = financasService;
    }

    @Operation(summary = "Resumo financeiro por período")
    @GetMapping("/resumo")
    public FinancasResumoResponse resumo(
            @RequestParam(value = "dataInicio", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(value = "dataFim", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return financasService.obterResumo(dataInicio, dataFim);
    }
}
