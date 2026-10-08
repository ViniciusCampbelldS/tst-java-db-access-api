package br.com.tst.dto;

import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;

public record EpiRequest(

    @NotBlank(message = "Informe o CA.")
    @Size(max = 15, message = "O CA deve possuir no máximo 15 caracteres.")
    String ca,

    @NotBlank(message = "Informe o lote.")
    @Size(max = 20, message = "O lote deve possuir no máximo 20 caracteres.")
    String lote,

    @NotBlank(message = "Informe o nome do epi.")
    @Size(max = 600, message = "O nome deve possuir no máximo 600 caracteres.")
    String nome,

    @NotNull(message = "Informe o vencimento.")
    Instant vencimento,

    boolean substituido,

    List<@NotNull @Positive Long> funcionarioIds

    ) {
}
