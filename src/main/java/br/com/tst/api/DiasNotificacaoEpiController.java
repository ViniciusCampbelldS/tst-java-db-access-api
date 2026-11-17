package br.com.tst.api;
import br.com.tst.model.DiasNotificacaoEpi;
import br.com.tst.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;
@RestController @RequestMapping("/dias-notificacao-epis")
public class DiasNotificacaoEpiController {
 private final DiasNotificacaoEpiRepository repository; private final NRRepository nrs;
 public DiasNotificacaoEpiController(DiasNotificacaoEpiRepository repository,NRRepository nrs){this.repository=repository;this.nrs=nrs;}
 public record Request(@NotNull @Min(1) Integer ca,@NotNull @Positive Integer diasAviso){}
 @GetMapping public List<DiasNotificacaoEpi> listar(){return repository.findAll();}
 @GetMapping("/{id}") public DiasNotificacaoEpi buscar(@PathVariable Long id){return repository.findById(id).orElseThrow(()->new br.com.tst.service.RegistroNaoEncontradoException("Regra não encontrada."));}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public DiasNotificacaoEpi criar(@Valid @RequestBody Request r){return salvar(null,r);}
 @PutMapping("/{id}") public DiasNotificacaoEpi atualizar(@PathVariable Long id,@Valid @RequestBody Request r){return salvar(id,r);}
 private DiasNotificacaoEpi salvar(Long id,Request r){
 
 if(id==null?repository.existsByCa(r.ca()):repository.existsByCaAndIdNot(r.ca(),id))throw new IllegalArgumentException("Regra já cadastrada.");
 var e=id==null?new DiasNotificacaoEpi():buscar(id);e.setCa(r.ca());e.setDiasAviso(r.diasAviso());return repository.save(e);
 }
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluir(@PathVariable Long id){repository.delete(buscar(id));}
}
