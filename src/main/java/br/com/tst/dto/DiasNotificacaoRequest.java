package br.com.tst.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DiasNotificacaoRequest(
    @NotBlank(message = "Informe o CA ou número da NR.")
    @Size(max = 15, message = "O CA ou número da NR deve possuir no máximo 15 caracteres.")
    String caOuNr,

    boolean isNorma,

    @NotNull(message = "Informe a quantidade de dias antes do aviso.")
    @Positive(message = "A quantidade de dias deve ser maior que zero.")
    Integer diasAviso
) {
}
