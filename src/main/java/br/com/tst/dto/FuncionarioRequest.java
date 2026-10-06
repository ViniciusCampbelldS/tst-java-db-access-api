package br.com.tst.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FuncionarioRequest(
    @NotBlank(message = "Informe o nome do(a) funcionario.")
    @Size(max = 100, message = "O nome deve possuir no máximo 100 caracteres.")
    String nome,

    @Size(max = 40, message = "O cargo deve possuir no máximo 40 caracteres.")
    String cargo,

    @NotBlank(message = "Informe o CPF.")
    @Pattern(
        regexp = "^(?:\\D*\\d){11}\\D*$",
        message = "O CPF deve possuir exatamente 11 números."
    )
    String cpf,

    @Size(max = 40, message = "O setor deve possuir no máximo 40 caracteres.")
    String setor
) {
}
