package br.com.tst.repository;
public interface DiasNotificacaoTreinamentoRepository extends org.springframework.data.jpa.repository.JpaRepository<br.com.tst.model.DiasNotificacaoTreinamento,Long> { boolean existsByNrAndIdNot(Integer v,Long id); boolean existsByNr(Integer v); }
