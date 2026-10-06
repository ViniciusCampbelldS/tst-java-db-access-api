package br.com.tst.form;

import br.com.tst.model.Epi;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EpiForm {

    private Long id;

    @NotBlank(message = "Informe o nome do epi.")
    @Size(max = 100, message = "O nome deve possuir no máximo 100 caracteres.")
    private String nome;

    @Size(max = 255, message = "A descrição deve possuir no máximo 255 caracteres.")
    private String descricao;

    @NotNull(message = "Informe o preço.")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
    private BigDecimal preco;

    @NotNull(message = "Informe a quantidade.")
    @Min(value = 0, message = "A quantidade não pode ser negativa.")
    private Integer quantidade;

    @NotEmpty(message = "Selecione ao menos uma funcionario.")
    private List<@NotNull @Positive Long> funcionarioIds = new ArrayList<>();

    public EpiForm() {
    }

    public static EpiForm from(Epi epi) {
        EpiForm form = new EpiForm();
        form.setId(epi.getId());
        form.setNome(epi.getNome());
        form.setDescricao(epi.getDescricao());
        form.setPreco(epi.getPreco());
        form.setQuantidade(epi.getQuantidade());
        form.setFuncionarioIds(
            epi.getFuncionarios().stream().map(funcionario -> funcionario.getId()).toList()
        );
        return form;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public List<Long> getFuncionarioIds() {
        return funcionarioIds;
    }

    public void setFuncionarioIds(List<Long> funcionarioIds) {
        this.funcionarioIds = funcionarioIds;
    }
}
