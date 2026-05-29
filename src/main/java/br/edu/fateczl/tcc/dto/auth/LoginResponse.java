package br.edu.fateczl.tcc.dto.auth;

import br.edu.fateczl.tcc.dto.funcionario.FuncionarioResponse;

public record LoginResponse(
        String token,
        FuncionarioResponse funcionario
) {
}
