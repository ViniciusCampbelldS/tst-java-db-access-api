package br.com.tst.repository;

import br.com.tst.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    Optional<Funcionario> findByNomeIgnoreCase(String nome);
}
