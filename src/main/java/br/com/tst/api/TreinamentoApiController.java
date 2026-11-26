package br.com.tst.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/treinamentos")
public class TreinamentoApiController {
    private final JdbcTemplate jdbc;
    private final br.com.tst.service.HistoricoService historico;

    public TreinamentoApiController(JdbcTemplate jdbc, br.com.tst.service.HistoricoService historico) {
        this.jdbc = jdbc;
        this.historico = historico;
    }

    private static final java.util.Set<String> TIPOS_DOCUMENTO = java.util.Set.of("application/pdf", "image/png", "image/jpeg");

    public record TurmaRequest(@NotNull @Positive Long nrId, @NotNull LocalDate data,
                               @NotNull LocalTime horario, @NotEmpty @Size(max = 100) List<@Positive Long> funcionarioIds) {}
    public record PresencaRequest(@NotNull Boolean presente) {}
    private static Map<String,Object> documentoSnap(String nome, String tipo, int tamanho) {
        var m = new java.util.LinkedHashMap<String,Object>();
        m.put("documentoNome", nome); m.put("tipo", tipo); m.put("tamanhoBytes", tamanho);
        return m;
    }

    private Map<String,Object> documentoAtual(long turmaId) {
        var rows = jdbc.queryForList("select documento_nome,documento_tipo_conteudo,length(documento_conteudo) as tamanho from turmas_treinamento where id=? for update", turmaId);
        if (rows.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada.");
        var r = rows.getFirst();
        return r.get("documento_nome") == null ? null : documentoSnap((String) r.get("documento_nome"), (String) r.get("documento_tipo_conteudo"), ((Number) r.get("tamanho")).intValue());
    }

    /** Envia ou substitui o documento da turma (PDF, PNG ou JPEG de até 5 MB). */
    @PutMapping("/turmas/{turmaId}/documento")
    @org.springframework.transaction.annotation.Transactional
    public Map<String,Object> enviarDocumento(@PathVariable long turmaId, @RequestParam("arquivo") org.springframework.web.multipart.MultipartFile arquivo) throws java.io.IOException {
        var tipo = arquivo.getContentType();
        if (arquivo.isEmpty() || tipo == null || !TIPOS_DOCUMENTO.contains(tipo)) throw new IllegalArgumentException("Envie um documento PDF, PNG ou JPEG.");
        if (arquivo.getSize() > 5L * 1024 * 1024) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "O documento deve ter até 5 MB.");
        var antes = documentoAtual(turmaId);
        var conteudo = arquivo.getBytes();
        var nome = arquivo.getOriginalFilename() == null || arquivo.getOriginalFilename().isBlank() ? "documento" : arquivo.getOriginalFilename().replaceAll("[\\\\/]", "_");
        if (nome.length() > 255) nome = nome.substring(0, 255);
        jdbc.update("update turmas_treinamento set documento_nome=?,documento_tipo_conteudo=?,documento_conteudo=? where id=?", nome, tipo, conteudo, turmaId);
        var depois = documentoSnap(nome, tipo, conteudo.length);
        historico.registrar("TURMA", turmaId, antes == null ? "DOCUMENTO_ENVIO" : "DOCUMENTO_ALTERACAO", antes, depois);
        return depois;
    }

    @GetMapping("/turmas/{turmaId}/documento")
    public org.springframework.http.ResponseEntity<byte[]> baixarDocumento(@PathVariable long turmaId) {
        var rows = jdbc.queryForList("select documento_nome,documento_tipo_conteudo,documento_conteudo from turmas_treinamento where id=? and documento_conteudo is not null", turmaId);
        if (rows.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado.");
        var r = rows.getFirst();
        return org.springframework.http.ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType((String) r.get("documento_tipo_conteudo")))
                .header("Content-Disposition", org.springframework.http.ContentDisposition.attachment().filename((String) r.get("documento_nome"), java.nio.charset.StandardCharsets.UTF_8).build().toString())
                .body((byte[]) r.get("documento_conteudo"));
    }

    @DeleteMapping("/turmas/{turmaId}/documento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @org.springframework.transaction.annotation.Transactional
    public void apagarDocumento(@PathVariable long turmaId) {
        var antes = documentoAtual(turmaId);
        if (antes == null) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado.");
        jdbc.update("update turmas_treinamento set documento_nome=null,documento_tipo_conteudo=null,documento_conteudo=null where id=?", turmaId);
        historico.registrar("TURMA", turmaId, "DOCUMENTO_EXCLUSAO", antes, null);
    }
    private static Map<String,Object> presencaSnap(Map<String,Object> r, long funcionarioId) {
        var m = new java.util.LinkedHashMap<String,Object>();
        m.put("_funcionarioId", funcionarioId);
        m.put("presente", r.get("presente"));
        m.put("concluidoEm", r.get("concluido_em") == null ? null : r.get("concluido_em").toString());
        m.put("vencimento", r.get("vencimento") == null ? null : r.get("vencimento").toString());
        return m;
    }

    @GetMapping("/turmas")
    public List<Map<String,Object>> turmas() {
        return jdbc.queryForList("select t.id,t.nr_id as \"nrId\",n.validade_meses as \"validadeMeses\",n.nome as \"nrNome\",t.data,t.horario,t.documento_nome as \"documentoNome\",p.funcionario_id as \"funcionarioId\",f.nome as funcionario,p.presente,p.vencimento from turmas_treinamento t join nrs n on n.id=t.nr_id join presencas_treinamento p on p.turma_id=t.id join funcionarios f on f.id=p.funcionario_id order by t.data desc,t.id desc");
    }

    @PostMapping("/turmas")
    @ResponseStatus(HttpStatus.CREATED)
    @org.springframework.transaction.annotation.Transactional
    public Map<String,Object> criarTurma(@Valid @RequestBody TurmaRequest request) {
        var ids=request.funcionarioIds().stream().distinct().sorted().toList();
        if(ids.size()!=request.funcionarioIds().size()) throw new IllegalArgumentException("Funcionários repetidos na turma.");
        for(var id:ids) if(jdbc.queryForObject("select count(*) from funcionarios where id=? and status <> 'In'",Integer.class,id)==0) throw new IllegalArgumentException("Funcionário inexistente ou inativo.");
        if (jdbc.queryForObject("select count(*) from nrs where id=?", Integer.class, request.nrId()) == 0) {
            throw new IllegalArgumentException("NR não cadastrada.");
        }
        var turma=jdbc.queryForMap("insert into turmas_treinamento(nr_id,data,horario) values(?,?,?) returning id,nr_id as \"nrId\",data,horario",request.nrId(),request.data(),request.horario());
        long turmaId=((Number)turma.get("id")).longValue();
        for(var id:ids) jdbc.update("insert into presencas_treinamento(turma_id,funcionario_id,presente) values(?,?,false)",turmaId,id);
        historico.registrar("TURMA", turmaId, "CRIACAO", null, Map.of("nrId", request.nrId(), "data", request.data().toString(), "horario", request.horario().toString(), "funcionarioIds", ids));
        return turma;
    }

    @PatchMapping("/turmas/{turmaId}/participantes/{funcionarioId}")
    @org.springframework.transaction.annotation.Transactional
    public void presenca(@PathVariable long turmaId,@PathVariable long funcionarioId,@Valid @RequestBody PresencaRequest request,Authentication sessao) {
        var estadoSql="select presente,concluido_em,vencimento from presencas_treinamento where turma_id=? and funcionario_id=?";
        var antes=jdbc.queryForList(estadoSql+" for update",turmaId,funcionarioId);
        if(antes.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND);
        jdbc.update("""
            update presencas_treinamento set presente=?,
            concluido_em=case when ? then current_date else null end,
            vencimento=case when ? then current_date + (select cast(round(n.validade_meses*30.436875) as integer) from nrs n join turmas_treinamento t on t.nr_id=n.id where t.id=?) else null end
            where turma_id=? and funcionario_id=?
            """,request.presente(),request.presente(),request.presente(),turmaId,turmaId,funcionarioId);
        historico.registrar("TURMA", turmaId, "PRESENCA", presencaSnap(antes.getFirst(), funcionarioId), presencaSnap(jdbc.queryForMap(estadoSql,turmaId,funcionarioId), funcionarioId));
    }
}
