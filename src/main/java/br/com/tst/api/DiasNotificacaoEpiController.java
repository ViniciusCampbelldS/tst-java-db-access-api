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
 private final DiasNotificacaoEpiRepository repository; private final NRRepository nrs; private final br.com.tst.service.HistoricoService historico;
 public DiasNotificacaoEpiController(DiasNotificacaoEpiRepository repository,NRRepository nrs,br.com.tst.service.HistoricoService historico){this.repository=repository;this.nrs=nrs;this.historico=historico;}
 public record Request(@NotNull @Min(1) Integer ca,@NotNull @Positive Integer diasAviso){}
 @GetMapping public List<DiasNotificacaoEpi> listar(){return repository.findAll();}
 @GetMapping("/{id}") public DiasNotificacaoEpi buscar(@PathVariable Long id){return repository.findById(id).orElseThrow(()->new br.com.tst.service.RegistroNaoEncontradoException("Regra não encontrada."));}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @org.springframework.transaction.annotation.Transactional  public DiasNotificacaoEpi criar(@Valid @RequestBody Request r){return salvar(null,r);}
 @PutMapping("/{id}") @org.springframework.transaction.annotation.Transactional  public DiasNotificacaoEpi atualizar(@PathVariable Long id,@Valid @RequestBody Request r){return salvar(id,r);}
 private DiasNotificacaoEpi salvar(Long id,Request r){
 
 if(id==null?repository.existsByCa(r.ca()):repository.existsByCaAndIdNot(r.ca(),id))throw new IllegalArgumentException("Regra já cadastrada.");
 var e=id==null?new DiasNotificacaoEpi():buscar(id);var antes=id==null?null:regra(e);e.setCa(r.ca());e.setDiasAviso(r.diasAviso());var salvo=repository.save(e);historico.registrar("NOTIFICACAO_EPI",salvo.getId(),id==null?"CRIACAO":"EDICAO",antes,regra(salvo));return salvo;
 }
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @org.springframework.transaction.annotation.Transactional  public void excluir(@PathVariable Long id){var e=buscar(id);var antes=regra(e);repository.delete(e);historico.registrar("NOTIFICACAO_EPI",id,"EXCLUSAO",antes,null);}
 private static java.util.Map<String,Object> regra(DiasNotificacaoEpi e){return java.util.Map.of("ca",e.getCa(),"diasAviso",e.getDiasAviso());}
}
