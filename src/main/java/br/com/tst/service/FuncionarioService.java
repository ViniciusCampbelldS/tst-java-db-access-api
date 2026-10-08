package br.com.tst.service;

import br.com.tst.dto.FuncionarioPatchRequest;
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
     * Atualiza somente os campos informados.
     */
    @Transactional
    public Funcionario atualizarParcial(Long id, FuncionarioPatchRequest request) {
        Funcionario funcionario = buscar(id);

        if (request.nome() != null) {
            funcionario.setNome(request.nome());
        }
        if (request.cargo() != null) {
            funcionario.setCargo(request.cargo());
        }
        if (request.cpf() != null) {
            funcionario.setCpf(request.cpf());
        }
        if (request.setor() != null) {
            funcionario.setSetor(request.setor());
        }
        if (request.permissoes() != null) {
            funcionario.setPermissoes(request.permissoes());
        }
        if (request.status() != null) {
            funcionario.setStatus(request.status());
        }
        if (request.nRs() != null) {
            funcionario.setnRs(request.nRs());
        }

        return salvar(funcionario);
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