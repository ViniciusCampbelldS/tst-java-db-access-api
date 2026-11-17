package br.com.tst.api;
import br.com.tst.service.EntregaEpiService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.time.LocalDate;
@RestController @RequestMapping("/entregas-epis")
public class EntregaEpiController {
 private final EntregaEpiService service;public EntregaEpiController(EntregaEpiService s){service=s;}
 public record Request(@NotNull @Positive Long funcionarioId,@NotNull LocalDate dataEntrega,@NotEmpty @Size(max=100)List<@NotNull @Positive Long> epiIds){}
 @GetMapping public List<EntregaEpiService.Entrega> listar(@RequestParam(required=false)Long funcionarioId){return service.listar(funcionarioId);}
 @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.CREATED)public EntregaEpiService.Entrega entregar(@Valid @RequestBody Request r,Authentication a){return service.entregar(r.funcionarioId(),r.dataEntrega(),r.epiIds(),Long.valueOf(a.getName()));}
}
