package br.com.tst.repository;

import br.com.tst.model.NR;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NRRepository extends JpaRepository<NR, Long> {

    java.util.Optional<NR> findByNome(String nome);

    boolean existsByNome(String nome);
}
