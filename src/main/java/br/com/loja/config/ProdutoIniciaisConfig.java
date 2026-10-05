package br.com.loja.config;

import br.com.loja.model.Categoria;
import br.com.loja.model.Produto;
import br.com.loja.repository.CategoriaRepository;
import br.com.loja.repository.ProdutoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class ProdutoIniciaisConfig {

    @Bean
    CommandLineRunner carregarProdutos(ProdutoRepository produtoRepository,
                                       CategoriaRepository categoriaRepository) {
        return args -> {
            if (produtoRepository.count() == 0) {
                Produto produtoInformatica = new Produto(
                    "NR11",
                    "Segurança para o transporte, a movimentação, a armazenagem e o manuseio de materiais.",
                    new BigDecimal("11.11"),
                    16,
                    List.of()
                );
                Produto produtoEscritorio = new Produto(
                    "NR13",
                    "Norma Regulamentadora do Ministério do Trabalho e Emprego (MTE) que estabelece os requisitos mínimos para a gestão da integridade estrutural de caldeiras, vasos de pressão, tubulações e tanques metálicos de armazenamento.",
                    new BigDecimal("14.44"),
                    12,
                    List.of()
                );

                produtoRepository.saveAll(List.of(produtoInformatica, produtoEscritorio));
            }
        };
    }

    private Categoria buscarOuCriarCategoria(CategoriaRepository repository,
                                              String nome,
                                              String descricao) {
        return repository.findByNomeIgnoreCase(nome)
            .orElseGet(() -> repository.save(new Categoria(nome, descricao)));
    }
}
