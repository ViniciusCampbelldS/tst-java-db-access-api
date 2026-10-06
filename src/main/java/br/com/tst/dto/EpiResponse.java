package br.com.tst.dto;

import br.com.tst.model.Epi;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

public record EpiResponse(
        Long id,
        String ca,
        String lote,
        String nome,
        Instant vencimento,
        boolean substituido,
        List<FuncionarioResponse> funcionarios
) {
 public  static EpiResponse from (Epi epi){
   return new EpiResponse(
           epi.getId(),
           epi.getCa(),
           epi.getLote(),
           epi.getNome(),
           epi.getVencimento().atStartOfDay(ZoneOffset.UTC).toInstant(),
           epi.isSubstituido(),
           epi.getFuncionarios().stream().map(FuncionarioResponse::from).toList()
   );
 }

}
