package br.com.tst.api;

import br.com.tst.service.HistoricoService;
import java.time.LocalDate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/historico")
public class HistoricoController {
    private static final Set<String> AREAS = Set.of("EPI", "NR", "TREINAMENTO", "TURMA", "FUNCIONARIO", "NOTIFICACAO_EPI", "NOTIFICACAO_TREINAMENTO");
    private final HistoricoService service;

    public HistoricoController(HistoricoService service) {
        this.service = service;
    }

    @GetMapping
    public HistoricoService.Pagina listar(@RequestParam(required = false) String areaDoSite,
                                          @RequestParam(required = false) Long registroId,
                                          @RequestParam(required = false) Long autorId,
                                          @RequestParam(required = false) LocalDate de,
                                          @RequestParam(required = false) LocalDate ate,
                                          @RequestParam(defaultValue = "false") boolean agrupar,
                                          @RequestParam(defaultValue = "0") int pagina,
                                          @RequestParam(defaultValue = "20") int tamanho) {
        if (areaDoSite != null && !areaDoSite.isBlank() && !AREAS.contains(areaDoSite.toUpperCase())) {
            throw new IllegalArgumentException("Área do site inválida.");
        }
        return service.listar(areaDoSite == null || areaDoSite.isBlank() ? null : areaDoSite.toUpperCase(), registroId, autorId, de, ate, agrupar, pagina, tamanho);
    }

    public record ComentarioRequest(@NotBlank @Size(max = 1000) String texto) {}

    /** Acrescenta uma nota ao evento e devolve todas as notas dele. */
    @PostMapping("/{id}/comentarios")
    public List<String> comentar(@PathVariable long id, @Valid @RequestBody ComentarioRequest request) {
        return service.comentar(id, request.texto());
    }
}
