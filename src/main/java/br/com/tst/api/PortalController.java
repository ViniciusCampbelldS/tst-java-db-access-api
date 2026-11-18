package br.com.tst.api;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/portal")
public class PortalController {
 private static final String POSSE="WITH atuais AS (select i.epi_id,d.funcionario_id from entrega_epi_itens i join entregas_epi d on d.id=i.entrega_id where i.encerrado_em is null) ";
 private final JdbcTemplate jdbc;public PortalController(JdbcTemplate j){jdbc=j;}
 @GetMapping("/epis") public List<Map<String,Object>> epis(Authentication a){return jdbc.queryForList(POSSE+"select e.id,e.ca,e.nome,e.vencimento,e.substituido,coalesce(d.dias_aviso,30) as \"diasAviso\" from epis e join atuais f on f.epi_id=e.id left join dias_notificacao_epis d on d.ca=e.ca where f.funcionario_id=?",Long.valueOf(a.getName()));}
 @GetMapping("/treinamentos") public List<Map<String,Object>> treinamentos(Authentication a){return jdbc.queryForList("select t.id,concat('NR-',lpad(cast(t.nr_id as varchar(10)),2,'0')) as nr,t.nome,t.data,p.vencimento,p.presente,coalesce(d.dias_aviso,30) as \"diasAviso\" from turmas_treinamento t join presencas_treinamento p on p.turma_id=t.id left join dias_notificacao_treinamentos d on d.nr=t.nr_id where p.funcionario_id=?",Long.valueOf(a.getName()));}
 public record Relato(@NotNull @Positive Long epiId,@NotBlank @Size(max=100) String situacao,@Size(max=4000)String comentario){}
 @PostMapping("/relatos") @ResponseStatus(HttpStatus.CREATED) public void relato(@Valid @RequestBody Relato r,Authentication a){Long id=Long.valueOf(a.getName());
 Integer owns=jdbc.queryForObject(POSSE+"select count(*) from atuais where epi_id=? and funcionario_id=?",Integer.class,r.epiId(),id);
 if(owns==null||owns==0)throw new ResponseStatusException(HttpStatus.FORBIDDEN);
 jdbc.update("insert into relatos_epi(epi_id,funcionario_id,situacao,comentario,criado_em) values(?,?,?,?,CURRENT_TIMESTAMP)",r.epiId(),id,r.situacao(),r.comentario());
 }
}
