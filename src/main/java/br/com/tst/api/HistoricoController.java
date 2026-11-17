package br.com.tst.api;

import br.com.tst.service.HistoricoService;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/historico")
public class HistoricoController {
    private static final Set<String> AREAS = Set.of("EPI", "TREINAMENTO", "TURMA");
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
                                          @RequestParam(defaultValue = "0") int pagina,
                                          @RequestParam(defaultValue = "20") int tamanho) {
        if (areaDoSite != null && !areaDoSite.isBlank() && !AREAS.contains(areaDoSite.toUpperCase())) {
            throw new IllegalArgumentException("Área do site inválida.");
        }
        return service.listar(areaDoSite == null || areaDoSite.isBlank() ? null : areaDoSite.toUpperCase(), registroId, autorId, de, ate, pagina, tamanho);
    }
}
