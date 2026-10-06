package br.com.tst.repository;

import br.com.tst.model.DiasNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiasNotificacaoRepository extends JpaRepository<DiasNotificacao, Long> {

    boolean existsByCaOuNrAndIsNorma(String caOuNr, boolean isNorma);

    boolean existsByCaOuNrAndIsNormaAndIdNot(String caOuNr, boolean isNorma, Long id);
}
