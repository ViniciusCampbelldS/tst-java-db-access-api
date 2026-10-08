package br.com.tst.service;

import br.com.tst.model.Funcionario;
import br.com.tst.repository.EpiRepository;
import br.com.tst.repository.FuncionarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Regras de negócio relacionadas aos funcionários.
 */
@Service
public class FuncionarioService {

    // Repositório de funcionários.
    private final FuncionarioRepository funcionarioRepository;

    // Repositório de EPIs.
    // É utilizado para impedir a exclusão de funcionários
    // que ainda possuem EPIs vinculados.
    private final EpiRepository epiRepository;

    // Construtor
    public FuncionarioService(
            FuncionarioRepository funcionarioRepository,
            EpiRepository epiRepository
    ) {
        this.funcionarioRepository = funcionarioRepository;
        this.epiRepository = epiRepository;
    }

    /**
     * Lista todos os funcionários.
     */
    @Transactional(readOnly = true)
    public List<Funcionario> listar() {

        return funcionarioRepository.findAll();
    }

    /**
     * Busca um funcionário pelo ID.
     */
    @Transactional(readOnly = true)
    public Funcionario buscar(Long id) {

        return funcionarioRepository
                .findById(id)
                .orElseThrow(() ->
                        new RegistroNaoEncontradoException(
                                "Funcionário não encontrado."
                        )
                );
    }

    /**
     * Cria ou atualiza um funcionário.
     */
    @Transactional
    public Funcionario salvar(Funcionario funcionario) {

        // Remove espaços desnecessários do nome.
        funcionario.setNome(
                funcionario.getNome().trim()
        );

        // Mantém somente os números do CPF.
        funcionario.setCpf(
                funcionario.getCpf().replaceAll("\\D", "")
        );

        // Persiste no banco.
        return funcionarioRepository.save(funcionario);
    }

    /**
     * Exclui um funcionário.
     */
    @Transactional
    public void excluir(Long id) {

        // Garante que o funcionário realmente existe.
        buscar(id);

        // Remove o registro.
        funcionarioRepository.deleteById(id);
    }
}