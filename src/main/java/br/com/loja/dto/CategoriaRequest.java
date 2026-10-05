package br.com.loja.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
    @NotBlank(message = "Informe o nome da categoria.")
    @Size(max = 80, message = "O nome deve possuir no máximo 80 caracteres.")
    String nome,

    @Size(max = 255, message = "A descrição deve possuir no máximo 255 caracteres.")
    String descricao
) {
}
