package br.com.tst.service;

import br.com.tst.model.DiasNotificacao;
import br.com.tst.repository.DiasNotificacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiasNotificacaoService {

    private final DiasNotificacaoRepository repository;

    public DiasNotificacaoService(DiasNotificacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<DiasNotificacao> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public DiasNotificacao buscar(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RegistroNaoEncontradoException(
                "Configuração de dias de notificação não encontrada."
            ));
    }

    @Transactional
    public DiasNotificacao salvar(DiasNotificacao diasNotificacao) {
        diasNotificacao.setCaOuNr(diasNotificacao.getCaOuNr().trim());
        boolean duplicado = diasNotificacao.getId() == null
            ? repository.existsByCaOuNrAndIsNorma(
                diasNotificacao.getCaOuNr(),
                diasNotificacao.isNorma()
            )
            : repository.existsByCaOuNrAndIsNormaAndIdNot(
                diasNotificacao.getCaOuNr(),
                diasNotificacao.isNorma(),
                diasNotificacao.getId()
            );
        if (duplicado) {
            throw new IllegalArgumentException(
                "Já existe uma configuração para esse CA ou número da NR e tipo."
            );
        }
        return repository.save(diasNotificacao);
    }

    @Transactional
    public void excluir(Long id) {
        buscar(id);
        repository.deleteById(id);
    }
}
