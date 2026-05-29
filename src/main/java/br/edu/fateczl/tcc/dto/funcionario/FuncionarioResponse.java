package br.edu.fateczl.tcc.dto.funcionario;

import java.time.LocalDateTime;

public record FuncionarioResponse(
        Long id,
        String nome,
        String email,
        boolean emailVerificado,
        boolean ativo,
        LocalDateTime criadoEm
) {
}
