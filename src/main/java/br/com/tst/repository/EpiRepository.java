package br.com.tst.repository;

import br.com.tst.model.Epi;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EpiRepository extends JpaRepository<Epi, Long> {

    @Override
    @EntityGraph(attributePaths = "funcionarios")
    List<Epi> findAll(Sort sort);

    @Query("select distinct p from Epi p left join fetch p.funcionarios where p.id = :id")
    Optional<Epi> buscarComFuncionarios(@Param("id") Long id);

    boolean existsByFuncionarios_Id(Long funcionarioId);
}
