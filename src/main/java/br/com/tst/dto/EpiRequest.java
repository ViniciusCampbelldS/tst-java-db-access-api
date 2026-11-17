package br.com.tst.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record EpiRequest(

    @jakarta.validation.constraints.Positive
    @jakarta.validation.constraints.NotNull Integer ca,

    @NotBlank(message = "Informe o lote.")
    @Size(max = 20, message = "O lote deve possuir no máximo 20 caracteres.")
    String lote,

    @NotBlank(message = "Informe o nome do epi.")
    @Size(max = 600, message = "O nome deve possuir no máximo 600 caracteres.")
    String nome,

    @NotNull(message = "Informe o vencimento.")
    LocalDate vencimento,

    boolean substituido,

    List<@NotNull @Positive Long> funcionarioIds

    ) {
}
