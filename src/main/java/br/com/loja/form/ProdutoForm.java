package br.com.loja.form;

import br.com.loja.model.Produto;
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

public class ProdutoForm {

    private Long id;

    @NotBlank(message = "Informe o nome do produto.")
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

    @NotEmpty(message = "Selecione ao menos uma categoria.")
    private List<@NotNull @Positive Long> categoriaIds = new ArrayList<>();

    public ProdutoForm() {
    }

    public static ProdutoForm from(Produto produto) {
        ProdutoForm form = new ProdutoForm();
        form.setId(produto.getId());
        form.setNome(produto.getNome());
        form.setDescricao(produto.getDescricao());
        form.setPreco(produto.getPreco());
        form.setQuantidade(produto.getQuantidade());
        form.setCategoriaIds(
            produto.getCategorias().stream().map(categoria -> categoria.getId()).toList()
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

    public List<Long> getCategoriaIds() {
        return categoriaIds;
    }

    public void setCategoriaIds(List<Long> categoriaIds) {
        this.categoriaIds = categoriaIds;
    }
}
