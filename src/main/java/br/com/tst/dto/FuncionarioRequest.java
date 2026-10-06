package br.com.tst.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

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
    String setor,

    @NotBlank(message = "Informe as permissões do(a) funcionario.")
    @Pattern(regexp = "ADM|field|tst", message = "As permissões devem ser ADM, field ou tst.")
    String permicoes,

    @NotBlank(message = "Informe o status do(a) funcionario.")
    @Pattern(regexp = "At|Af|In", message = "O status deve ser At, Af ou In.")
    String status,

    @NotNull(message = "Informe a lista de NRs.")
    @JsonProperty("nRs")
    @JsonAlias({"NRs", "nrs"})
    List<@NotBlank(message = "Cada NR deve possuir um valor.")
         @Size(max = 15, message = "Cada NR deve possuir no máximo 15 caracteres.") String> nRs
) {
}
