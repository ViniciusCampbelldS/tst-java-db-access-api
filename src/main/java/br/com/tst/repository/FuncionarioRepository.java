package br.com.tst.repository;

import br.com.tst.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    java.util.Optional<Funcionario> findByCpf(String cpf);
    java.util.Optional<Funcionario> findByEmailIgnoreCase(String email);
    java.util.Optional<Funcionario> findBySessaoTokenHash(String hash);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    java.util.Optional<Funcionario> findByRecuperacaoTokenHash(String hash);
}
