package br.com.tst.api;

import br.com.tst.dto.NRRequest;
import br.com.tst.dto.NRResponse;
import br.com.tst.model.NR;
import br.com.tst.repository.NRRepository;
import br.com.tst.service.HistoricoService;
import br.com.tst.service.RegistroNaoEncontradoException;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/nrs")
public class NRApiController {

    private final NRRepository nrRepository;
    private final HistoricoService historico;

    public NRApiController(NRRepository nrRepository, HistoricoService historico) {
        this.nrRepository = nrRepository;
        this.historico = historico;
    }

    @GetMapping
    public List<NRResponse> listar() {
        return nrRepository.findAll().stream()
                .map(NRResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public NRResponse buscar(@PathVariable Long id) {
        return NRResponse.from(buscarNr(id));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<NRResponse> cadastrar(@Valid @RequestBody NRRequest request) {
        if (nrRepository.existsById(request.id())) throw new IllegalArgumentException("Número de NR já cadastrado.");
        NR nr = nrRepository.save(new NR(request.id(), request.nome().trim()));
        historico.registrar("NR", nr.getId(), "CRIACAO", null, Map.of("nome", nr.getNome()));
        return ResponseEntity.created(URI.create("/nrs/" + nr.getId()))
                .body(NRResponse.from(nr));
    }

    @PutMapping("/{id}")
    @Transactional
    public NRResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody NRRequest request
    ) {
        if (!id.equals(request.id())) throw new IllegalArgumentException("O número da NR não pode ser alterado.");
        NR nr = buscarNr(id);
        var antes = Map.<String, Object>of("nome", nr.getNome());
        nr.setNome(request.nome());
        var salvo = nrRepository.save(nr);
        historico.registrar("NR", salvo.getId(), "EDICAO", antes, Map.of("nome", salvo.getNome()));
        return NRResponse.from(salvo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void excluir(@PathVariable Long id) {
        NR nr = buscarNr(id);
        var antes = Map.<String, Object>of("nome", nr.getNome());
        nrRepository.delete(nr);
        historico.registrar("NR", id, "EXCLUSAO", antes, null);
    }

    private NR buscarNr(Long id) {
        return nrRepository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException(
                        "NR não encontrada: " + id
                ));
    }
}
