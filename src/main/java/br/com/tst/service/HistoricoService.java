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

    public record Comentario(String texto, java.time.Instant criadoEm) {}

    public record Evento(Long id, Long autorId, String autorNome, java.time.Instant criadoEm, String areaDoSite,
                         Long registroId, String acao, Object antes, Object depois, List<Comentario> comentarios) {}

    public record Pagina(List<Evento> itens, long total, int pagina, int tamanho) {}

    /** Grava na transação da operação; autor sempre vem da sessão autenticada. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(String areaDoSite, long registroId, String acao, Map<String, ?> antes, Map<String, ?> depois) {
        if (antes != null && depois != null) {
            var a = apenasAlterados(antes, depois);
            var d = apenasAlterados(depois, antes);
            if (a.isEmpty() && d.isEmpty()) return;
            antes = comContexto(a, antes);
            depois = comContexto(d, depois);
        }
        jdbc.update("insert into historico_alteracoes(autor_id,area_do_site,registro_id,acao,antes,depois) values(?,?,?,?,cast(? as jsonb),cast(? as jsonb))",
                autor(), areaDoSite, registroId, acao, serializar(antes), serializar(depois));
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
    public Pagina listar(String areaDoSite, Long registroId, Long autorId, java.time.LocalDate de, java.time.LocalDate ate, int pagina, int tamanho) {
        tamanho = Math.min(Math.max(tamanho, 1), 100);
        pagina = Math.max(pagina, 0);
        var where = new StringBuilder(" where 1=1");
        var args = new ArrayList<Object>();
        if (areaDoSite != null && !areaDoSite.isBlank()) { where.append(" and h.area_do_site=?"); args.add(areaDoSite); }
        if (registroId != null) { where.append(" and h.registro_id=?"); args.add(registroId); }
        if (autorId != null) { where.append(" and h.autor_id=?"); args.add(autorId); }
        if (de != null) { where.append(" and h.criado_em >= ?"); args.add(java.sql.Timestamp.from(de.atStartOfDay(java.time.ZoneId.of("America/Sao_Paulo")).toInstant())); }
        if (ate != null) { where.append(" and h.criado_em < ?"); args.add(java.sql.Timestamp.from(ate.plusDays(1).atStartOfDay(java.time.ZoneId.of("America/Sao_Paulo")).toInstant())); }
        Long total = jdbc.queryForObject("select count(*) from historico_alteracoes h" + where, Long.class, args.toArray());
        var paginaArgs = new ArrayList<>(args);
        paginaArgs.add(tamanho);
        paginaArgs.add((long) pagina * tamanho);
        var linhas = jdbc.query("select h.id,h.autor_id,f.nome autor_nome,h.criado_em,h.area_do_site,h.registro_id,h.acao,h.antes,h.depois"
                + " from historico_alteracoes h left join funcionarios f on f.id=h.autor_id" + where + " order by h.id desc limit ? offset ?",
                (rs, n) -> new Linha(rs.getLong("id"), (Long) rs.getObject("autor_id"), rs.getString("autor_nome"),
                        rs.getTimestamp("criado_em").toInstant(), rs.getString("area_do_site"), rs.getLong("registro_id"),
                        rs.getString("acao"), ler(rs.getString("antes")), ler(rs.getString("depois"))), paginaArgs.toArray());
        if (linhas.isEmpty()) return new Pagina(List.of(), total == null ? 0 : total, pagina, tamanho);

        var ids = linhas.stream().map(Linha::id).toList();
        var comentarios = new java.util.HashMap<Long, List<Comentario>>();
        jdbc.query("select historico_id,texto,criado_em from historico_comentarios where historico_id in ("
                + String.join(",", java.util.Collections.nCopies(ids.size(), "?")) + ") order by id",
                rs -> { comentarios.computeIfAbsent(rs.getLong("historico_id"), k -> new ArrayList<>())
                        .add(new Comentario(rs.getString("texto"), rs.getTimestamp("criado_em").toInstant())); }, ids.toArray());

        var itens = linhas.stream().map(l -> new Evento(l.id(), l.autorId(), l.autorNome(), l.criadoEm(), l.areaDoSite(),
                l.registroId(), l.acao(), l.antes(), l.depois(), comentarios.getOrDefault(l.id(), List.of()))).toList();
        return new Pagina(itens, total == null ? 0 : total, pagina, tamanho);
    }
    /** Acrescenta uma nota ao evento; as notas não podem ser editadas nem removidas. */
    @Transactional
    public List<Comentario> comentar(long eventoId, String texto) {
        if (texto == null || texto.isBlank() || texto.length() > 1000) throw new IllegalArgumentException("Informe uma nota de até 1000 caracteres.");
        if (jdbc.queryForObject("select count(*) from historico_alteracoes where id=?", Integer.class, eventoId) == 0) {
            throw new RegistroNaoEncontradoException("Evento do histórico não encontrado.");
        }
        jdbc.update("insert into historico_comentarios(historico_id,autor_id,texto) values(?,?,?)", eventoId, autor(), texto.trim());
        return jdbc.query("select texto,criado_em from historico_comentarios where historico_id=? order by id", (rs, n) -> new Comentario(rs.getString("texto"), rs.getTimestamp("criado_em").toInstant()), eventoId);
    }

    private record Linha(Long id, Long autorId, String autorNome, java.time.Instant criadoEm, String areaDoSite,
                         Long registroId, String acao, Object antes, Object depois) {}

    private Object ler(String s) {
        if (s == null) return null;
        try {
            var no = json.readTree(s);
            return no.isTextual() ? json.readTree(no.asText()) : no;
        } catch (JsonProcessingException e) { return s; }
    }}
