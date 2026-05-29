package br.edu.fateczl.tcc.controller;

import br.edu.fateczl.tcc.dto.funcionario.FuncionarioRequest;
import br.edu.fateczl.tcc.dto.funcionario.FuncionarioResponse;
import br.edu.fateczl.tcc.dto.funcionario.FuncionarioUpdateRequest;
import br.edu.fateczl.tcc.service.FuncionarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/funcionarios")
@Tag(name = "Funcionario Controller", description = "Gestão de funcionários")
public class FuncionarioController {

    private final FuncionarioService service;

    public FuncionarioController(FuncionarioService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastrar funcionário")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FuncionarioResponse criar(@Valid @RequestBody FuncionarioRequest request) {
        return service.criar(request);
    }

    @Operation(summary = "Listar funcionários com paginação")
    @GetMapping
    public Page<FuncionarioResponse> listar(
            @RequestParam(value = "busca", required = false) String busca,
            @RequestParam(value = "pagina", defaultValue = "0") int pagina,
            @RequestParam(value = "tamanho", defaultValue = "10") int tamanho
    ) {
        return service.listar(busca, pagina, tamanho);
    }

    @Operation(summary = "Buscar funcionário por ID")
    @GetMapping("/{id}")
    public FuncionarioResponse buscarPorId(@PathVariable("id") Long id) {
        return service.buscarPorId(id);
    }

    @Operation(summary = "Atualizar funcionário")
    @PutMapping("/{id}")
    public FuncionarioResponse atualizar(
            @PathVariable("id") Long id,
            @Valid @RequestBody FuncionarioUpdateRequest request
    ) {
        return service.atualizar(id, request);
    }

    @Operation(summary = "Desativar funcionário")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable("id") Long id) {
        service.desativar(id);
    }
}
