package br.com.tst.service;

import br.com.tst.form.EpiForm;
import br.com.tst.model.Epi;
import br.com.tst.model.Funcionario;
import br.com.tst.repository.EpiRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EpiService {

    private final EpiRepository epiRepository;
    private final FuncionarioService funcionarioService;

    public EpiService(EpiRepository epiRepository,
                      FuncionarioService funcionarioService) {
        this.epiRepository = epiRepository;
        this.funcionarioService = funcionarioService;
    }

    @Transactional(readOnly = true)
    public List<Epi> listar() {
        return epiRepository.findAll(Sort.by("nome").ascending());
    }

    @Transactional(readOnly = true)
    public Epi buscar(Long id) {
        return epiRepository.buscarComFuncionarios(id)
            .orElseThrow(() -> new RegistroNaoEncontradoException(
                "Epi não encontrado."
            ));
    }

    @Transactional
    public Epi salvar(EpiForm form) {
        List<Funcionario> funcionarios = form.getFuncionarioIds().stream()
            .distinct()
            .map(funcionarioService::buscar)
            .toList();

        Epi epi = form.getId() == null
            ? new Epi()
            : buscar(form.getId());

        epi.setCa(form.getCa().trim());
        epi.setLote(form.getLote().trim());
        epi.setNome(form.getNome().trim());
        epi.setVencimento(form.getVencimento());
        epi.setFuncionarios(funcionarios);

        return epiRepository.save(epi);
    }

    @Transactional
    public void excluir(Long id) {
        buscar(id);
        epiRepository.deleteById(id);
    }
}
