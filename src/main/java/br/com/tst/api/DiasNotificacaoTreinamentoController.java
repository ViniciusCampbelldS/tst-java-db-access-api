package br.com.tst.api;
import br.com.tst.model.DiasNotificacaoTreinamento;
import br.com.tst.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;
@RestController @RequestMapping("/dias-notificacao-treinamentos")
public class DiasNotificacaoTreinamentoController {
 private final DiasNotificacaoTreinamentoRepository repository; private final NRRepository nrs;
 public DiasNotificacaoTreinamentoController(DiasNotificacaoTreinamentoRepository repository,NRRepository nrs){this.repository=repository;this.nrs=nrs;}
 public record Request(@NotNull @Min(0) Integer nr,@NotNull @Positive Integer diasAviso){}
 @GetMapping public List<DiasNotificacaoTreinamento> listar(){return repository.findAll();}
 @GetMapping("/{id}") public DiasNotificacaoTreinamento buscar(@PathVariable Long id){return repository.findById(id).orElseThrow(()->new br.com.tst.service.RegistroNaoEncontradoException("Regra não encontrada."));}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public DiasNotificacaoTreinamento criar(@Valid @RequestBody Request r){return salvar(null,r);}
 @PutMapping("/{id}") public DiasNotificacaoTreinamento atualizar(@PathVariable Long id,@Valid @RequestBody Request r){return salvar(id,r);}
 private DiasNotificacaoTreinamento salvar(Long id,Request r){
 if (!nrs.existsById(r.nr().longValue())) throw new IllegalArgumentException("NR não cadastrada.");
 if(id==null?repository.existsByNr(r.nr()):repository.existsByNrAndIdNot(r.nr(),id))throw new IllegalArgumentException("Regra já cadastrada.");
 var e=id==null?new DiasNotificacaoTreinamento():buscar(id);e.setNr(r.nr());e.setDiasAviso(r.diasAviso());return repository.save(e);
 }
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluir(@PathVariable Long id){repository.delete(buscar(id));}
}
