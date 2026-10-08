package br.com.tst.api;

import br.com.tst.dto.EpiBulkCreateResponse;
import br.com.tst.dto.EpiCreateRequest;
import br.com.tst.dto.EpiPatchRequest;
import br.com.tst.dto.EpiRequest;
import br.com.tst.dto.EpiResponse;
import br.com.tst.form.EpiForm;
import br.com.tst.model.Epi;
import br.com.tst.service.EpiService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/epis")
public class EpiApiController {

    private final EpiService epiService;

    public EpiApiController(EpiService epiService) {
        this.epiService = epiService;
    }

    // Listar todos Epis
    @GetMapping
    public List<EpiResponse> list() {
        return epiService.listar().stream().map(EpiResponse::from).toList();
    }

    // Procurar epi por ID
    @GetMapping("/{id}")
    public EpiResponse buscar(@PathVariable Long id) {
        return EpiResponse.from(epiService.buscar(id));
    }

    // Create/Insert INDIVIDUAL - POST localhost:8080/epis
    @PostMapping
    public ResponseEntity<EpiResponse> cadastrar(
            @Valid @RequestBody EpiRequest request) {

        Epi epi = epiService.salvar(
                converterParaForm(null, request)
        );

        EpiResponse response = EpiResponse.from(epi);

        return ResponseEntity.created(
                URI.create("/epis/" + epi.getId())
        ).body(response);
    }

    // Create/Insert EM LOTE - POST localhost:8080/epis/bulk

    @PostMapping("/bulk")
    public ResponseEntity<EpiBulkCreateResponse> cadastrarEmLote(
            @Valid @RequestBody EpiCreateRequest request) {

        // Passa a lista e a referência do seu método existente 'converterParaForm'
        List<Epi> episSalvos =
                epiService.salvarEmLote(
                        request,
                        this::converterParaForm
                );

        List<EpiResponse> itensResponse =
                episSalvos.stream()
                        .map(EpiResponse::from)
                        .toList();

        EpiBulkCreateResponse response =
                new EpiBulkCreateResponse(itensResponse);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    //	Update = Replaces an entire resource - PUT /epis/{id}
    @PutMapping("/{id}")
    public EpiResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody EpiRequest request) {
        Epi epi = epiService.salvar(
                converterParaForm(id, request)
        );

        return EpiResponse.from(epi);
    }

    @PatchMapping("/{id}")
    public EpiResponse atualizarParcial(
            @PathVariable Long id,
            @Valid @RequestBody EpiPatchRequest request) {
        return EpiResponse.from(epiService.atualizarParcial(id, request));
    }


    //	DELETE /epis/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        epiService.excluir(id);
    }


    private EpiForm converterParaForm(Long id, EpiRequest epiRequest) {
        // Cria o objeto intermediário utilizado pelo service.
        EpiForm form = new EpiForm();
        // Define o ID; null representa um novo registro.
        form.setId(id);
        // Copia o Certificado de Aprovação.
        form.setCa(epiRequest.ca());
        // Copia o lote.
        form.setLote(epiRequest.lote());
        // Copia o nome.
        form.setNome(epiRequest.nome());
        // Copia a data diretamente como LocalDate.
        // Não converte para Instant nem aplica fuso horário.
        form.setVencimento(epiRequest.vencimento());
        // Copia o estado de substituição.
        form.setSubstituido(epiRequest.substituido());
        // Copia os IDs dos funcionários relacionados.
        form.setFuncionarioIds(epiRequest.funcionarioIds());
        // Devolve o objeto preenchido.
        return form;
    }
}
