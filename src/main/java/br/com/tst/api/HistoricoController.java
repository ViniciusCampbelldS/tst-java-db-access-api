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
    private static final Set<String> ENTIDADES = Set.of("EPI", "TREINAMENTO", "TURMA");
    private final HistoricoService service;

    public HistoricoController(HistoricoService service) {
        this.service = service;
    }

    @GetMapping
    public HistoricoService.Pagina listar(@RequestParam(required = false) String entidade,
                                          @RequestParam(required = false) Long registroId,
                                          @RequestParam(required = false) Long autorId,
                                          @RequestParam(required = false) LocalDate de,
                                          @RequestParam(required = false) LocalDate ate,
                                          @RequestParam(defaultValue = "0") int pagina,
                                          @RequestParam(defaultValue = "20") int tamanho) {
        if (entidade != null && !entidade.isBlank() && !ENTIDADES.contains(entidade.toUpperCase())) {
            throw new IllegalArgumentException("Entidade inválida.");
        }
        return service.listar(entidade == null || entidade.isBlank() ? null : entidade.toUpperCase(), registroId, autorId, de, ate, pagina, tamanho);
    }
}
