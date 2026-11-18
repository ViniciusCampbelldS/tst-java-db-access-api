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

    private static Map<String,Object> curso(Map<String,Object> r) {
        var m = new java.util.LinkedHashMap<String,Object>();
        m.put("nrId", r.get("nrId")); m.put("nome", r.get("nome")); m.put("validadeMeses", r.get("validadeMeses"));
        return m;
    }

    public record CursoRequest(@NotNull @Positive Long nrId, @NotBlank @Size(max = 250) String nome,
                               @NotNull @Positive Integer validadeMeses) {}
    public record TurmaRequest(@NotNull @Positive Long treinamentoId, @NotNull LocalDate data,
                               @NotNull LocalTime horario, @NotEmpty @Size(max = 100) List<@Positive Long> funcionarioIds) {}
    public record PresencaRequest(@NotNull Boolean presente) {}

    @GetMapping
    public List<Map<String,Object>> listar() {
        return jdbc.queryForList("select id, nr_id as \"nrId\", nome, validade_meses as \"validadeMeses\" from treinamentos order by id desc");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @org.springframework.transaction.annotation.Transactional
    public Map<String,Object> criar(@Valid @RequestBody CursoRequest request) {
        if (jdbc.queryForObject("select count(*) from nrs where id=?", Integer.class, request.nrId()) == 0) {
            throw new IllegalArgumentException("NR não cadastrada.");
        }
        var novo = jdbc.queryForMap("insert into treinamentos(nr_id,nome,validade_meses) values(?,?,?) returning id,nr_id as \"nrId\",nome,validade_meses as \"validadeMeses\"",
                request.nrId(), request.nome().trim(), request.validadeMeses());
        historico.registrar("TREINAMENTO", ((Number) novo.get("id")).longValue(), "CRIACAO", null, curso(novo));
        return novo;
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public Map<String,Object> atualizar(@PathVariable long id, @Valid @RequestBody CursoRequest request) {
        if (jdbc.queryForObject("select count(*) from nrs where id=?", Integer.class, request.nrId()) == 0) {
            throw new IllegalArgumentException("NR não cadastrada.");
        }
        var anteriores=jdbc.queryForList("select nr_id as \"nrId\",nome,validade_meses as \"validadeMeses\" from treinamentos where id=? for update",id);
        if(anteriores.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND);
        var rows=jdbc.queryForList("update treinamentos set nr_id=?,nome=?,validade_meses=? where id=? returning id,nr_id as \"nrId\",nome,validade_meses as \"validadeMeses\"",request.nrId(),request.nome().trim(),request.validadeMeses(),id);
        if(rows.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND);
        historico.registrar("TREINAMENTO", id, "EDICAO", curso(anteriores.getFirst()), curso(rows.getFirst()));
        return rows.getFirst();
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
        return jdbc.queryForList("select t.id,t.treinamento_id as \"treinamentoId\",c.nr_id as \"nrId\",c.validade_meses as \"validadeMeses\",n.nome as \"nrNome\",c.nome as treinamento,t.data,t.horario,p.funcionario_id as \"funcionarioId\",f.nome as funcionario,p.presente from turmas_treinamento t join treinamentos c on c.id=t.treinamento_id join nrs n on n.id=c.nr_id join presencas_treinamento p on p.turma_id=t.id join funcionarios f on f.id=p.funcionario_id order by t.data desc,t.id desc");
    }

    @PostMapping("/turmas")
    @ResponseStatus(HttpStatus.CREATED)
    @org.springframework.transaction.annotation.Transactional
    public Map<String,Object> criarTurma(@Valid @RequestBody TurmaRequest request) {
        var ids=request.funcionarioIds().stream().distinct().sorted().toList();
        if(ids.size()!=request.funcionarioIds().size()) throw new IllegalArgumentException("Funcionários repetidos na turma.");
        for(var id:ids) if(jdbc.queryForObject("select count(*) from funcionarios where id=? and status <> 'In'",Integer.class,id)==0) throw new IllegalArgumentException("Funcionário inexistente ou inativo.");
        if (jdbc.queryForObject("select count(*) from treinamentos where id=?", Integer.class, request.treinamentoId()) == 0) {
            throw new IllegalArgumentException("Treinamento não cadastrado.");
        }
        var turma=jdbc.queryForMap("""
            insert into turmas_treinamento(treinamento_id,nr_id,data,horario,nome)
            select c.id,c.nr_id,?,?,c.nome
            from treinamentos c where c.id=?
            returning id,treinamento_id as \"treinamentoId\",data,horario
            """,request.data(),request.horario(),request.treinamentoId());
        long turmaId=((Number)turma.get("id")).longValue();
        for(var id:ids) jdbc.update("insert into presencas_treinamento(turma_id,funcionario_id,presente) values(?,?,false)",turmaId,id);
        historico.registrar("TURMA", turmaId, "CRIACAO", null, Map.of("treinamentoId", request.treinamentoId(), "data", request.data().toString(), "horario", request.horario().toString(), "funcionarioIds", ids));
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
            vencimento=case when ? then (current_date + ((select c.validade_meses from treinamentos c join turmas_treinamento t on t.treinamento_id=c.id where t.id=?) || ' months')::interval)::date else null end
            where turma_id=? and funcionario_id=?
            """,request.presente(),request.presente(),request.presente(),turmaId,turmaId,funcionarioId);
        historico.registrar("TURMA", turmaId, "PRESENCA", presencaSnap(antes.getFirst(), funcionarioId), presencaSnap(jdbc.queryForMap(estadoSql,turmaId,funcionarioId), funcionarioId));
    }
}
