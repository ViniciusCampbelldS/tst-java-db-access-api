package br.com.tst.api;

import br.com.tst.dto.FuncionarioRequest;
import br.com.tst.dto.FuncionarioResponse;
import br.com.tst.model.Funcionario;
import br.com.tst.service.FuncionarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/funcionarios")
public class FuncionarioApiController {

    private final FuncionarioService funcionarioService;

    public FuncionarioApiController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping
    public List<FuncionarioResponse> listar() {
        return funcionarioService.listar().stream().map(FuncionarioResponse::from).toList();
    }

    @GetMapping("/{id}")
    public FuncionarioResponse buscar(@PathVariable Long id) {
        return FuncionarioResponse.from(funcionarioService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<FuncionarioResponse> cadastrar(
            @Valid @RequestBody FuncionarioRequest request) {
        Funcionario funcionario =
            new Funcionario(request.nome(), request.cargo(), request.cpf(), request.setor());
        funcionario.setPermissoes(request.permicoes());
        funcionario.setStatus(request.status());
        funcionario.setnRs(request.nRs());
        funcionario = funcionarioService.salvar(funcionario);
        FuncionarioResponse response = FuncionarioResponse.from(funcionario);
        return ResponseEntity
            .created(URI.create("/api/funcionarios/" + funcionario.getId()))
            .body(response);
    }

    @PutMapping("/{id}")
    public FuncionarioResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody FuncionarioRequest request) {
        Funcionario funcionario = funcionarioService.buscar(id);
        funcionario.setNome(request.nome());
        funcionario.setCargo(request.cargo());
        funcionario.setCpf(request.cpf());
        funcionario.setSetor(request.setor());
        funcionario.setPermissoes(request.permicoes());
        funcionario.setStatus(request.status());
        funcionario.setnRs(request.nRs());
        return FuncionarioResponse.from(funcionarioService.salvar(funcionario));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        funcionarioService.excluir(id);
    }
}
