package br.com.tst.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistoricoService {
    private static final Set<String> SENSIVEIS = Set.of("senha", "senhahash", "senha_hash", "token", "tokenhash", "hash");
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;

    public HistoricoService(JdbcTemplate jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    public record Evento(Long id, Long autorId, String autorNome, java.time.Instant criadoEm, String entidade,
                         Long registroId, String acao, Object antes, Object depois) {}

    public record Pagina(List<Evento> itens, long total, int pagina, int tamanho) {}

    /** Grava na transação da operação; autor sempre vem da sessão autenticada. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(String entidade, long registroId, String acao, Map<String, ?> antes, Map<String, ?> depois) {
        if (antes != null && depois != null) {
            var a = apenasAlterados(antes, depois);
            var d = apenasAlterados(depois, antes);
            if (a.isEmpty() && d.isEmpty()) return;
            antes = comContexto(a, antes);
            depois = comContexto(d, depois);
        }
        jdbc.update("insert into historico_alteracoes(autor_id,entidade,registro_id,acao,antes,depois) values(?,?,?,?,cast(? as jsonb),cast(? as jsonb))",
                autor(), entidade, registroId, acao, serializar(antes), serializar(depois));
    }

    /** Chaves iniciadas por "_" são contexto (ex.: _funcionarioId): ficam sempre, sem o prefixo, e não contam como alteração. */
    private Map<String, Object> comContexto(Map<String, Object> alterados, Map<String, ?> origem) {
        var r = new LinkedHashMap<String, Object>();
        origem.forEach((k, v) -> { if (k.startsWith("_")) r.put(k.substring(1), v); });
        r.putAll(alterados);
        return r;
    }

    private Map<String, Object> apenasAlterados(Map<String, ?> a, Map<String, ?> b) {
        var r = new LinkedHashMap<String, Object>();
        for (var e : a.entrySet()) {
            if (!e.getKey().startsWith("_") && !java.util.Objects.equals(e.getValue(), b.get(e.getKey()))) r.put(e.getKey(), e.getValue());
        }
        return r;
    }

    private Long autor() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) return null;
        try { return Long.valueOf(a.getName()); } catch (NumberFormatException e) { return null; }
    }

    private String serializar(Map<String, ?> dados) {
        if (dados == null) return null;
        var limpo = new LinkedHashMap<String, Object>();
        dados.forEach((k, v) -> { if (!SENSIVEIS.contains(k.toLowerCase())) limpo.put(k, v); });
        try { return json.writeValueAsString(limpo); } catch (JsonProcessingException e) { throw new IllegalStateException(e); }
    }

    @Transactional(readOnly = true)
    public Pagina listar(String entidade, Long registroId, Long autorId, java.time.LocalDate de, java.time.LocalDate ate, int pagina, int tamanho) {
        tamanho = Math.min(Math.max(tamanho, 1), 100);
        pagina = Math.max(pagina, 0);
        var where = new StringBuilder(" where 1=1");
        var args = new ArrayList<Object>();
        if (entidade != null && !entidade.isBlank()) { where.append(" and h.entidade=?"); args.add(entidade); }
        if (registroId != null) { where.append(" and h.registro_id=?"); args.add(registroId); }
        if (autorId != null) { where.append(" and h.autor_id=?"); args.add(autorId); }
        if (de != null) { where.append(" and h.criado_em >= ?"); args.add(java.sql.Timestamp.from(de.atStartOfDay(java.time.ZoneId.of("America/Sao_Paulo")).toInstant())); }
        if (ate != null) { where.append(" and h.criado_em < ?"); args.add(java.sql.Timestamp.from(ate.plusDays(1).atStartOfDay(java.time.ZoneId.of("America/Sao_Paulo")).toInstant())); }
        Long total = jdbc.queryForObject("select count(*) from historico_alteracoes h" + where, Long.class, args.toArray());
        var paginaArgs = new ArrayList<>(args);
        paginaArgs.add(tamanho);
        paginaArgs.add((long) pagina * tamanho);
        var itens = jdbc.query("select h.id,h.autor_id,f.nome autor_nome,h.criado_em,h.entidade,h.registro_id,h.acao,h.antes,h.depois from historico_alteracoes h left join funcionarios f on f.id=h.autor_id"
                + where + " order by h.criado_em desc,h.id desc limit ? offset ?", (rs, n) -> new Evento(
                rs.getLong("id"), (Long) rs.getObject("autor_id"), rs.getString("autor_nome"),
                rs.getTimestamp("criado_em").toInstant(), rs.getString("entidade"), rs.getLong("registro_id"),
                rs.getString("acao"), ler(rs.getString("antes")), ler(rs.getString("depois"))), paginaArgs.toArray());
        return new Pagina(itens, total == null ? 0 : total, pagina, tamanho);
    }

    private Object ler(String s) {
        if (s == null) return null;
        try {
            var no = json.readTree(s);
            return no.isTextual() ? json.readTree(no.asText()) : no;
        } catch (JsonProcessingException e) { return s; }
    }
}
