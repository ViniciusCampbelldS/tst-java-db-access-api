package br.com.tst.api;

import br.com.tst.service.FichaEntrega;
import br.com.tst.service.RegistroNaoEncontradoException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/fichas-epi")
public class FichaEntregaController {
    private static final Set<String> PAPEIS_GESTAO = Set.of("ROLE_ADM", "ROLE_tst", "ROLE_gerencia");
    private final JdbcTemplate jdbc;

    public FichaEntregaController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record Resumo(long id, String nome, LocalDate dataEntrega) {}

    @GetMapping
    public List<Resumo> minhasFichas(Authentication sessao) {
        return jdbc.query("""
            select e.id, f.nome, e.data_entrega
            from fichas_entrega_epi f join entregas_epi e on e.id = f.entrega_id
            where e.funcionario_id = ?
            order by e.id desc
            """, (rs, n) -> new Resumo(rs.getLong("id"), rs.getString("nome"),
                                        rs.getObject("data_entrega", LocalDate.class)),
            Long.valueOf(sessao.getName()));
    }

    @GetMapping("/{entregaId}")
    public ResponseEntity<byte[]> baixar(@PathVariable long entregaId, Authentication sessao) {
        boolean podeGerenciar = sessao.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority).anyMatch(PAPEIS_GESTAO::contains);
        var arquivos = jdbc.query("""
            select f.nome, f.conteudo
            from fichas_entrega_epi f join entregas_epi e on e.id = f.entrega_id
            where e.id = ? and (e.funcionario_id = ? or ?)
            """, (rs, n) -> new FichaEntrega(rs.getString("nome"), rs.getBytes("conteudo")),
            entregaId, Long.valueOf(sessao.getName()), podeGerenciar);
        if (arquivos.isEmpty()) {
            throw new RegistroNaoEncontradoException("Ficha de entrega não encontrada.");
        }
        var ficha = arquivos.getFirst();
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .contentType(MediaType.APPLICATION_PDF)
            .headers(headers -> headers.setContentDisposition(
                ContentDisposition.attachment().filename(ficha.nome(), StandardCharsets.UTF_8).build()))
            .body(ficha.conteudo());
    }
}
