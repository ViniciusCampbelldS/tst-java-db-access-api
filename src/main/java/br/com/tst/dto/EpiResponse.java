package br.com.tst.dto;

import br.com.tst.model.Epi;
import java.time.LocalDate;
import java.util.List;

public record EpiResponse(
        Long id,
        @jakarta.validation.constraints.NotNull Integer ca,
        String lote,
        String nome,
        LocalDate vencimento,
        boolean substituido,
        java.time.Instant descartadoEm,
        java.time.LocalDate dataDescarte,
        String motivoDescarte,
        Long responsavelDescarteId,
        List<FuncionarioResponse> funcionarios
) {
    public static EpiResponse from(Epi epi) {
        return new EpiResponse(
                epi.getId(),
                epi.getCa(),
                epi.getLote(),
                epi.getNome(),
                epi.getVencimento(),
                epi.isSubstituido(),
                epi.getDescartadoEm(),epi.getDataDescarte(),epi.getMotivoDescarte(),epi.getResponsavelDescarteId(),
                epi.getFuncionarios().stream().map(FuncionarioResponse::from).toList()
        );
    }

}
