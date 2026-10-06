package br.com.tst.service;

import br.com.tst.model.Funcionario;
import br.com.tst.repository.FuncionarioRepository;
import br.com.tst.repository.EpiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final EpiRepository epiRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository,
                              EpiRepository epiRepository) {
        this.funcionarioRepository = funcionarioRepository;
        this.epiRepository = epiRepository;
    }

    @Transactional(readOnly = true)
    public List<Funcionario> listar() {
        return funcionarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Funcionario buscar(Long id) {
        return funcionarioRepository.findById(id)
            .orElseThrow(() -> new RegistroNaoEncontradoException(
                "Funcionario não encontrada."
            ));
    }

    @Transactional
    public Funcionario salvar(Funcionario funcionario) {
        funcionario.setNome(funcionario.getNome().trim());
        funcionario.setCpf(funcionario.getCpf().replaceAll("\\D", ""));

        return funcionarioRepository.save(funcionario);
    }

    @Transactional
    public void excluir(Long id) {
        buscar(id);

        if (epiRepository.existsByFuncionarios_Id(id)) {
            throw new IllegalStateException(
                "A funcionario possui epis vinculados e não pode ser excluída."
            );
        }

        funcionarioRepository.deleteById(id);
    }
}
