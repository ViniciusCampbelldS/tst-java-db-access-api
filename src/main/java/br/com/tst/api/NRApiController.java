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
        var novo = new NR(request.id(), request.nome().trim());
        novo.setValidadeMeses(request.validadeMeses());
        NR nr = nrRepository.save(novo);
        historico.registrar("NR", nr.getId(), "CRIACAO", null, snap(nr));
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
        var antes = snap(nr);
        nr.setNome(request.nome()); if (request.validadeMeses() != null) nr.setValidadeMeses(request.validadeMeses());
        var salvo = nrRepository.save(nr);
        historico.registrar("NR", salvo.getId(), "EDICAO", antes, snap(salvo));
        return NRResponse.from(salvo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void excluir(@PathVariable Long id) {
        NR nr = buscarNr(id);
        var antes = snap(nr);
        nrRepository.delete(nr);
        historico.registrar("NR", id, "EXCLUSAO", antes, null);
    }

    private static Map<String, Object> snap(NR nr) {
        var m = new java.util.LinkedHashMap<String, Object>();
        m.put("nome", nr.getNome());
        m.put("validadeMeses", nr.getValidadeMeses());
        return m;
    }

    private NR buscarNr(Long id) {
        return nrRepository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException(
                        "NR não encontrada: " + id
                ));
    }
}
