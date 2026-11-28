package br.com.tst.api;

import br.com.tst.service.HistoricoService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class EditarTurmaTest {
    static class Jdbc extends JdbcTemplate {
        int presencas;
        boolean existe = true;
        boolean atualizou;
        @Override public List<Map<String, Object>> queryForList(String sql, Object... args) {
            if (!existe) return List.of();
            return List.of(Map.of("nr_id", 1, "data", LocalDate.parse("2026-12-01"), "horario", LocalTime.parse("08:00")));
        }
        @Override public <T> T queryForObject(String sql, Class<T> type, Object... args) {
            return type.cast(sql.contains("presencas_treinamento") ? presencas : 1);
        }
        @Override public int update(String sql, Object... args) { atualizou = true; return 1; }
    }
    static class Historico extends HistoricoService {
        String acao;
        Historico() { super(null, null); }
        @Override public void registrar(String area, long id, String acao, Map<String, ?> antes, Map<String, ?> depois) { this.acao = acao; }
    }

    @Test void editaTurmaSemPresencaERegistraHistorico() {
        var jdbc = new Jdbc(); var historico = new Historico();
        var controller = new TreinamentoApiController(jdbc, historico);
        var resultado = controller.editarTurma(3, new TreinamentoApiController.EditarTurmaRequest(1L, LocalDate.parse("2026-12-02"), LocalTime.parse("09:00")));
        assertTrue(jdbc.atualizou);
        assertEquals("EDICAO", historico.acao);
        assertEquals(LocalDate.parse("2026-12-02"), resultado.get("data"));
    }

    @Test void bloqueiaAlteracaoDepoisDePresencaEParaTurmaInexistente() {
        var jdbc = new Jdbc(); var controller = new TreinamentoApiController(jdbc, new Historico());
        var request = new TreinamentoApiController.EditarTurmaRequest(1L, LocalDate.now(), LocalTime.NOON);
        jdbc.presencas = 1;
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class, () -> controller.editarTurma(3, request)).getStatusCode());
        assertFalse(jdbc.atualizou);
        jdbc.existe = false;
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class, () -> controller.editarTurma(3, request)).getStatusCode());
    }
}
