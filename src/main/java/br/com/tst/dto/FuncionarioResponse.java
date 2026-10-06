package br.com.tst.dto;

import br.com.tst.model.Funcionario;

public record FuncionarioResponse(
    Long id,
    String nome,
    String descricao
){
    public static FuncionarioResponse from(Funcionario funcionario){
        return new FuncionarioResponse(
          funcionario.getId(),
          funcionario.getNome(),
          funcionario.getDescricao()
        );
    }

}
