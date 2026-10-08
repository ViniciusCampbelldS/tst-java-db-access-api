package br.com.tst.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NRRequest(
        @NotBlank(message = "Informe o nome da NR.")
        @Size(max = 250, message = "O nome deve possuir no máximo 250 caracteres.")
        String nome
) {
}
