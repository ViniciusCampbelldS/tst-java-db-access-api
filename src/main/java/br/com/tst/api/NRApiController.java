package br.com.tst.api;

import br.com.tst.dto.NRRequest;
import br.com.tst.dto.NRResponse;
import br.com.tst.model.NR;
import br.com.tst.repository.NRRepository;
import br.com.tst.service.RegistroNaoEncontradoException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/nrs")
public class NRApiController {

    private final NRRepository nrRepository;

    public NRApiController(NRRepository nrRepository) {
        this.nrRepository = nrRepository;
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
    public ResponseEntity<NRResponse> cadastrar(@Valid @RequestBody NRRequest request) {
        NR nr = nrRepository.save(new NR(request.nome()));
        return ResponseEntity.created(URI.create("/nrs/" + nr.getId()))
                .body(NRResponse.from(nr));
    }

    @PutMapping("/{id}")
    public NRResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody NRRequest request
    ) {
        NR nr = buscarNr(id);
        nr.setNome(request.nome());
        return NRResponse.from(nrRepository.save(nr));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        nrRepository.delete(buscarNr(id));
    }

    private NR buscarNr(Long id) {
        return nrRepository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException(
                        "NR não encontrada: " + id
                ));
    }
}
