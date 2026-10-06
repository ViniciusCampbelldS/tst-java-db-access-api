package br.com.tst.api;

import br.com.tst.dto.DiasNotificacaoRequest;
import br.com.tst.dto.DiasNotificacaoResponse;
import br.com.tst.model.DiasNotificacao;
import br.com.tst.service.DiasNotificacaoService;
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
@RequestMapping("/api/dias-notificacao")
public class DiasNotificacaoApiController {

    private final DiasNotificacaoService service;

    public DiasNotificacaoApiController(DiasNotificacaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<DiasNotificacaoResponse> listar() {
        return service.listar().stream()
            .map(DiasNotificacaoResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    public DiasNotificacaoResponse buscar(@PathVariable Long id) {
        return DiasNotificacaoResponse.from(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<DiasNotificacaoResponse> cadastrar(
            @Valid @RequestBody DiasNotificacaoRequest request) {
        DiasNotificacao diasNotificacao = service.salvar(
            new DiasNotificacao(request.caOuNr(), request.isNorma(), request.diasAviso())
        );
        return ResponseEntity
            .created(URI.create("/api/dias-notificacao/" + diasNotificacao.getId()))
            .body(DiasNotificacaoResponse.from(diasNotificacao));
    }

    @PutMapping("/{id}")
    public DiasNotificacaoResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody DiasNotificacaoRequest request) {
        DiasNotificacao diasNotificacao = service.buscar(id);
        diasNotificacao.setCaOuNr(request.caOuNr());
        diasNotificacao.setNorma(request.isNorma());
        diasNotificacao.setDiasAviso(request.diasAviso());
        return DiasNotificacaoResponse.from(service.salvar(diasNotificacao));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
