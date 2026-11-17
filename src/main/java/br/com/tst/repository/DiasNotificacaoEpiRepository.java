package br.com.tst.repository;
public interface DiasNotificacaoEpiRepository extends org.springframework.data.jpa.repository.JpaRepository<br.com.tst.model.DiasNotificacaoEpi,Long> { boolean existsByCaAndIdNot(Integer v,Long id); boolean existsByCa(Integer v); }
