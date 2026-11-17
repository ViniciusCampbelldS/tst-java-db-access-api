package br.com.tst.api;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;
@RestController @RequestMapping("/epis/sugestoes")
public class EpiCatalogoController {
 private final JdbcTemplate jdbc;public EpiCatalogoController(JdbcTemplate jdbc){this.jdbc=jdbc;}
 public record Sugestao(Integer ca,String nome){}
 @GetMapping public List<Sugestao> listar(@RequestParam String ca){
  if(ca==null||!ca.matches("[0-9]{1,10}"))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Digite somente números no CA.");
  return jdbc.query("select distinct ca,nome from epis where cast(ca as varchar) like ? order by ca,nome limit 12",(rs,n)->new Sugestao(rs.getInt(1),rs.getString(2)),ca+"%");
 }
}
