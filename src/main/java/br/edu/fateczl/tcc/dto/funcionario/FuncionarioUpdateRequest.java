package br.edu.fateczl.tcc.dto.funcionario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FuncionarioUpdateRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 100, message = "E-mail deve ter no máximo 100 caracteres")
        String email,

        @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
        String senha
) {
}
