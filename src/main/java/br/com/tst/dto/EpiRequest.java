package br.com.tst.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record EpiRequest(

    @NotBlank(message = "Informe o nome do epi.")
    @Size(max = 100)
    String nome,

    @Size(max = 255)
    String descricao,

    @NotNull( message =  "Informe o preço.")
    @DecimalMin(
            value = "0.01",
            message = "O preço deve ser maior que zero."
    )
    BigDecimal preco,

    @NotNull(message = "Informe a quantidade.")
    @Min(
            value = 0,
            message = "A quantidade não pode ser negativa"
    )
    Integer quantidade,

    @NotEmpty(message = "Informe ao menos uma funcionario.")
    List<@NotNull @Positive Long> funcionarioIds

    ) {
}
