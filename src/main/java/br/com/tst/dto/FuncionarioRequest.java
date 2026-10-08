package br.com.tst.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Dados recebidos pela API quando um funcionário é criado ou atualizado. */
public record FuncionarioRequest(

        // Nome obrigatório
        @NotBlank(message = "Informe o nome do funcionário.")
        @Size(
                max = 100,
                message = "O nome deve possuir no máximo 100 caracteres."
        )
        String nome,

        // Cargo não obrigatório
        @Size(
                max = 40,
                message = "O cargo deve possuir no máximo 40 caracteres."
        )
        String cargo,

        // CPF obrigatório com exatamente 11 números.
        @NotBlank(message = "Informe o CPF.")
        @Pattern(
                // Ignora não números, verifica se são 11 números
                regexp = "^(?:\\D*\\d){11}\\D*$",
                message = "O CPF deve possuir exatamente 11 números."
        )
        String cpf,

        // Setor não obrigatório
        @Size(
                max = 40,
                message = "O setor deve possuir no máximo 40 caracteres."
        )
        String setor,

        // Permissão de acesso.
        //
        // ADM   = administrador
        // field = funcionário
        // tst   = técnico de segurança
        @NotBlank(message = "Informe as permissões do funcionário.")
        @Pattern(
                regexp = "ADM|field|tst",
                message = "As permissões devem ser ADM, field ou tst."
        )
        String permissoes,

        // Status do funcionário na empresa.
        //
        // At = Ativo
        // Af = Afastado
        // In = Inativo
        @NotBlank(message = "Informe o status do funcionário.")
        @Pattern(
                regexp = "At|Af|In",
                message = "O status deve ser At, Af ou In."
        )
        String status,

        // Lista de NRs relacionadas ao funcionário.
        @NotNull(message = "Informe a lista de NRs.")
        List<
                @NotBlank(message = "Cada NR deve possuir um valor.")
                @Size(
                        max = 250,
                        message = "Cada NR deve possuir no máximo 250 caracteres."
                )
                        String
                > nRs

) {
}