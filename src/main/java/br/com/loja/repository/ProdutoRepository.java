package br.com.loja.repository;

import br.com.loja.model.Produto;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @Override
    @EntityGraph(attributePaths = "categorias")
    List<Produto> findAll(Sort sort);

    @Query("select distinct p from Produto p left join fetch p.categorias where p.id = :id")
    Optional<Produto> buscarComCategorias(@Param("id") Long id);

    boolean existsByCategorias_Id(Long categoriaId);
}
