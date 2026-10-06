package br.com.tst.dto;

import br.com.tst.model.Funcionario;

public record FuncionarioResponse(
    Long id,
    String nome,
    String cargo,
    String cpf,
    String setor
){
    public static FuncionarioResponse from(Funcionario funcionario){
        return new FuncionarioResponse(
          funcionario.getId(),
          funcionario.getNome(),
          funcionario.getCargo(),
          funcionario.getCpf(),
          funcionario.getSetor()
        );
    }

}
