package br.com.tst.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FuncionarioPatchRequest(
        @Pattern(regexp = ".*\\S.*", message = "Informe o nome do funcionário.")
        @Size(max = 100, message = "O nome deve possuir no máximo 100 caracteres.")
        String nome,

        @Size(max = 40, message = "O cargo deve possuir no máximo 40 caracteres.")
        String cargo,

        @Pattern(
                regexp = "^(?:\\D*\\d){11}\\D*$",
                message = "O CPF deve possuir exatamente 11 números."
        )
        String cpf,

        @Size(max = 40, message = "O setor deve possuir no máximo 40 caracteres.")
        String setor,

        @Pattern(regexp = "ADM|field|tst", message = "As permissões devem ser ADM, field ou tst.")
        String permissoes,

        @Pattern(regexp = "At|Af|In", message = "O status deve ser At, Af ou In.")
        String status,

        List<
                @Pattern(
                        regexp = ".*\\S.*",
                        message = "Cada NR deve possuir um valor."
                )
                @NotNull(message = "Cada NR deve possuir um valor.")
                @Size(max = 250, message = "Cada NR deve possuir no máximo 250 caracteres.")
                String
                > nRs
) {
}
