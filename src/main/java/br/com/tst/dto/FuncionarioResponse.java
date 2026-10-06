package br.com.tst.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import br.com.tst.model.Funcionario;

import java.util.List;

public record FuncionarioResponse(
    Long id,
    String nome,
    String cargo,
    String cpf,
    String setor,
    String permicoes,
    String status,
    @JsonProperty("nRs") List<String> nRs
){
    public static FuncionarioResponse from(Funcionario funcionario){
        return new FuncionarioResponse(
          funcionario.getId(),
          funcionario.getNome(),
          funcionario.getCargo(),
          funcionario.getCpf(),
          funcionario.getSetor(),
          funcionario.getPermicoes(),
          funcionario.getStatus(),
          List.copyOf(funcionario.getnRs())
        );
    }

}
