package br.com.tst.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FuncionarioRequest(
    @NotBlank(message = "Informe o nome do(a) funcionario.")
    @Size(max = 80, message = "O nome deve possuir no máximo 80 caracteres.")
    String nome,

    @Size(max = 255, message = "A descrição deve possuir no máximo 255 caracteres.")
    String descricao
) {
}
