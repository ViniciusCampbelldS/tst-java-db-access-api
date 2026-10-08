package br.com.tst.dto;

import br.com.tst.model.Funcionario;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Dados devolvidos pela API para o Angular.
 */
public record FuncionarioResponse(
        Long id,
        String nome,
        String cargo,
        String cpf,
        String setor,
        String permissoes,
        // Status interno: At, Af ou In.
        String status,
        List<String> nRs

) {

    /**
     * Converte uma entidade JPA para o DTO enviado ao Angular.
     */
    public static FuncionarioResponse from(@NotNull Funcionario funcionario) {
        return new FuncionarioResponse(
                funcionario.getId(),
                funcionario.getNome(),
                funcionario.getCargo(),
                funcionario.getCpf(),
                funcionario.getSetor(),
                funcionario.getPermissoes(),
                funcionario.getStatus(),
                List.copyOf(funcionario.getnRs())
        );
    }
}