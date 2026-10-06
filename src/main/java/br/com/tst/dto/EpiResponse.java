package br.com.tst.dto;

import br.com.tst.model.Epi;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

public record EpiResponse(
        Long id,
        String ca,
        String nome,
        Instant vencimento,
        List<FuncionarioResponse> funcionarios
) {
 public  static EpiResponse from (Epi epi){
   return new EpiResponse(
           epi.getId(),
           epi.getCa(),
           epi.getNome(),
           epi.getVencimento().atStartOfDay(ZoneOffset.UTC).toInstant(),
           epi.getFuncionarios().stream().map(FuncionarioResponse::from).toList()
   );
 }

}
