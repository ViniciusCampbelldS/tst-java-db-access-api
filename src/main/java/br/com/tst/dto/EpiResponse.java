package br.com.tst.dto;

import br.com.tst.model.Epi;

import java.math.BigDecimal;
import java.util.List;

public record EpiResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer quantidade,
        List<FuncionarioResponse> funcionarios
) {
 public  static EpiResponse from (Epi epi){
   return new EpiResponse(
           epi.getId(),
           epi.getNome(),
           epi.getDescricao(),
           epi.getPreco(),
           epi.getQuantidade(),
           epi.getFuncionarios().stream().map(FuncionarioResponse::from).toList()
   );
 }

}
