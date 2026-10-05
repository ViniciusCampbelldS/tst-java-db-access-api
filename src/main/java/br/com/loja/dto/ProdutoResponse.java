package br.com.loja.dto;

import br.com.loja.model.Produto;

import java.math.BigDecimal;
import java.util.List;

public record ProdutoResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer quantidade,
        List<CategoriaResponse> categorias
) {
 public  static ProdutoResponse from (Produto produto){
   return new ProdutoResponse(
           produto.getId(),
           produto.getNome(),
           produto.getDescricao(),
           produto.getPreco(),
           produto.getQuantidade(),
           produto.getCategorias().stream().map(CategoriaResponse::from).toList()
   );
 }

}
