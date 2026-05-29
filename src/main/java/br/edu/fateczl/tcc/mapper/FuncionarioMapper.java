package br.edu.fateczl.tcc.mapper;

import br.edu.fateczl.tcc.domain.Funcionario;
import br.edu.fateczl.tcc.dto.funcionario.FuncionarioResponse;

public final class FuncionarioMapper {

    private FuncionarioMapper() {
    }

    public static FuncionarioResponse toResponse(Funcionario funcionario) {
        return new FuncionarioResponse(
                funcionario.getId(),
                funcionario.getNome(),
                funcionario.getEmail(),
                funcionario.isEmailVerificado(),
                funcionario.isAtivo(),
                funcionario.getCriadoEm()
        );
    }
}
