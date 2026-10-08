package br.com.tst.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record EpiCreateRequest(
        @NotEmpty(message = "A lista de EPIs não pode estar vazia.")
        @Valid
        List<EpiRequest> epis
) {}