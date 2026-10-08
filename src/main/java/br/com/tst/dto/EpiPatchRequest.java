package br.com.tst.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record EpiPatchRequest(
        @Pattern(regexp = ".*\\S.*", message = "Informe o CA.")
        @Size(max = 15, message = "O CA deve possuir no máximo 15 caracteres.")
        String ca,

        @Pattern(regexp = ".*\\S.*", message = "Informe o lote.")
        @Size(max = 20, message = "O lote deve possuir no máximo 20 caracteres.")
        String lote,

        @Pattern(regexp = ".*\\S.*", message = "Informe o nome do epi.")
        @Size(max = 600, message = "O nome deve possuir no máximo 600 caracteres.")
        String nome,

        LocalDate vencimento,

        Boolean substituido,

        List<@NotNull @Positive Long> funcionarioIds
) {
}
