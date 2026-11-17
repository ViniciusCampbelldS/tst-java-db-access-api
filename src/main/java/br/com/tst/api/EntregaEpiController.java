package br.com.tst.api;

import br.com.tst.service.EntregaEpiService;
import br.com.tst.service.FichaEntrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/entregas-epis")
public class EntregaEpiController {
    private final EntregaEpiService service;

    public EntregaEpiController(EntregaEpiService service) {
        this.service = service;
    }

    public record Request(@NotNull @Positive Long funcionarioId, @NotNull LocalDate dataEntrega,
                          @NotEmpty @Size(max = 100) List<@NotNull @Positive Long> epiIds) {}

    @GetMapping
    public List<EntregaEpiService.Entrega> listar(@RequestParam(required = false) Long funcionarioId) {
        return service.listar(funcionarioId);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EntregaEpiService.Entrega entregar(@Valid @RequestBody Request entrega, Authentication sessao) {
        return registrar(entrega, sessao, null);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EntregaEpiService.Entrega entregarComFicha(@Valid @RequestPart("entrega") Request entrega,
                                                      @RequestPart("ficha") MultipartFile arquivo,
                                                      Authentication sessao) throws IOException {
        if (arquivo.getSize() > FichaEntrega.LIMITE_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "A ficha deve ter até 5 MB.");
        }
        byte[] conteudo = arquivo.getBytes();
        if (conteudo.length < 5 || !"%PDF-".equals(new String(conteudo, 0, 5, StandardCharsets.US_ASCII))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma ficha em PDF.");
        }
        String nome = arquivo.getOriginalFilename();
        if (nome != null) {
            nome = nome.replace('\\', '/');
            nome = nome.substring(nome.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}\\p{Cf}]", "").strip();
        }
        if (nome == null || nome.length() <= 4 || nome.length() > 255 ||
                !nome.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um nome de arquivo PDF válido.");
        }
        return registrar(entrega, sessao, new FichaEntrega(nome, conteudo));
    }

    private EntregaEpiService.Entrega registrar(Request entrega, Authentication sessao, FichaEntrega ficha) {
        return service.entregar(entrega.funcionarioId(), entrega.dataEntrega(), entrega.epiIds(),
                                Long.valueOf(sessao.getName()), ficha);
    }
}
