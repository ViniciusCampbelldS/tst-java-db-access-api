package br.com.tst.api;

import br.com.tst.dto.FuncionarioRequest;
import br.com.tst.dto.FuncionarioResponse;
import br.com.tst.model.Funcionario;
import br.com.tst.service.FuncionarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controller REST responsável pelo CRUD de funcionários.
 */
@RestController
@RequestMapping("/funcionarios")
public class FuncionarioApiController {

    // Serviço responsável pelas regras de negócio.
    private final FuncionarioService funcionarioService;

    // Injeta o serviço de funcionários.
    public FuncionarioApiController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    /**
     * GET /funcionarios
     *
     * Retorna todos os funcionários.
     */
    @GetMapping
    public List<FuncionarioResponse> listar() {

        return funcionarioService.listar()
                .stream()
                // Converte cada entidade JPA para o DTO.
                .map(FuncionarioResponse::from)
                .toList();
    }

    /**
     * GET /funcionarios/{id}
     *
     * Retorna um funcionário específico.
     */
    @GetMapping("/{id}")
    public FuncionarioResponse buscar(@PathVariable Long id) {

        // Busca o funcionário e converte para DTO.
        return FuncionarioResponse.from(
                funcionarioService.buscar(id)
        );
    }

    /**
     * POST /funcionarios
     *
     * Cria um novo funcionário.
     */
    @PostMapping
    public ResponseEntity<FuncionarioResponse> cadastrar(
            @Valid @RequestBody FuncionarioRequest request
    ) {

        // Cria uma nova entidade utilizando os dados recebidos.
        Funcionario funcionario = new Funcionario(
                request.nome(),
                request.cargo(),
                request.cpf(),
                request.setor()
        );

        // Copia a permissão recebida do Angular.
        funcionario.setPermissoes(request.permissoes());

        // Copia o status recebido do Angular.
        funcionario.setStatus(request.status());

        // Copia a lista de NRs.
        funcionario.setnRs(request.nRs());

        // Persiste o funcionário no banco.
        funcionario = funcionarioService.salvar(funcionario);

        // Converte a entidade salva para DTO.
        FuncionarioResponse response =
                FuncionarioResponse.from(funcionario);

        // Retorna HTTP 201 Created.
        return ResponseEntity
                .created(
                        URI.create("/funcionarios/" + funcionario.getId())
                )
                .body(response);
    }

    /**
     * PUT /funcionarios/{id}
     *
     * Atualiza todos os dados de um funcionário existente.
     */
    @PutMapping("/{id}")
    public FuncionarioResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody FuncionarioRequest request
    ) {

        // Busca o funcionário existente.
        Funcionario funcionario =
                funcionarioService.buscar(id);

        // Atualiza os campos.
        funcionario.setNome(request.nome());
        funcionario.setCargo(request.cargo());
        funcionario.setCpf(request.cpf());
        funcionario.setSetor(request.setor());
        funcionario.setPermissoes(request.permissoes());
        funcionario.setStatus(request.status());
        funcionario.setnRs(request.nRs());

        // Salva novamente no banco.
        Funcionario atualizado =
                funcionarioService.salvar(funcionario);

        // Devolve o registro atualizado.
        return FuncionarioResponse.from(atualizado);
    }

    /**
     * DELETE /funcionarios/{id}
     *
     * Exclui um funcionário.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {

        // O service verifica se existem EPIs vinculados
        // antes de permitir a exclusão.
        funcionarioService.excluir(id);
    }
}