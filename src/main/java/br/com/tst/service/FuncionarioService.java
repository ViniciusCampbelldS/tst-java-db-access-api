package br.com.tst.service;

import br.com.tst.dto.FuncionarioPatchRequest;
import br.com.tst.model.Funcionario;
import br.com.tst.repository.EpiRepository;
import br.com.tst.repository.FuncionarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Regras de negócio relacionadas aos funcionários.
 */
@Service
public class FuncionarioService {

    // Repositório de funcionários.
    private final FuncionarioRepository funcionarioRepository;

    // Repositório de EPIs.
    // É utilizado para impedir a exclusão de funcionários
    // que ainda possuem EPIs vinculados.
    @org.springframework.beans.factory.annotation.Autowired private br.com.tst.repository.NRRepository nrRepository;
    private final EpiRepository epiRepository;
    @org.springframework.beans.factory.annotation.Autowired private HistoricoService historico;
    @org.springframework.beans.factory.annotation.Autowired private org.springframework.jdbc.core.JdbcTemplate jdbc;

    private static final List<String> OBSERVACOES_RETORNO = List.of(
            "NR-1: afastamento superior a 180 dias - realizar treinamento eventual antes de retomar a atividade das respectivas NRs.",
            "NR-7: afastamento de 30 dias ou mais - realizar Exame Médico de Retorno ao Trabalho (ASO) antes de reassumir a função.",
            "NR-10: afastamento superior a 3 meses (90 dias) - realizar treinamento de reciclagem completo.");

    // Construtor
    public FuncionarioService(
            FuncionarioRepository funcionarioRepository,
            EpiRepository epiRepository
    ) {
        this.funcionarioRepository = funcionarioRepository;
        this.epiRepository = epiRepository;
    }

    @Transactional(readOnly=true)
    public java.util.List<br.com.tst.dto.EpiResponse> meusEpis(Long id){return epiRepository.findAll().stream().filter(e->e.getFuncionarios().stream().anyMatch(f->f.getId().equals(id))).map(br.com.tst.dto.EpiResponse::from).toList();}
    /**
     * Lista todos os funcionários.
     */
    @Transactional(readOnly = true)
    public List<Funcionario> listar() {

        return funcionarioRepository.findAll();
    }

    /**
     * Busca um funcionário pelo ID.
     */
    @Transactional(readOnly = true)
    public Funcionario buscar(Long id) {

        return funcionarioRepository
                .findById(id)
                .orElseThrow(() ->
                        new RegistroNaoEncontradoException(
                                "Funcionário não encontrado."
                        )
                );
    }

    /**
     * Cria ou atualiza um funcionário.
     */
    @Transactional
    public Funcionario salvar(Funcionario funcionario) {

        // Remove espaços desnecessários do nome.
        funcionario.setNome(
                funcionario.getNome().trim()
        );

        // Mantém somente os números do CPF.
        funcionario.setCpf(
                funcionario.getCpf().replaceAll("\\D", "")
        );

        if (funcionario.getEmail() != null) funcionario.setEmail(funcionario.getEmail().trim().toLowerCase(java.util.Locale.ROOT));
        funcionario.setNormas(funcionario.getnRs().stream().distinct().map(nome -> nrRepository.findByNome(nome).orElseThrow(() -> new IllegalArgumentException("NR não cadastrada: " + nome))).toList());
        var dadosAnteriores = funcionario.getId() == null ? null : dadosPersistidos(funcionario.getId());
        var anterior = funcionario.getId() == null ? List.<String>of()
                : jdbc.queryForList("select status from funcionarios where id=?", String.class, funcionario.getId());
        var salvo = funcionarioRepository.save(funcionario);
        var dadosAtuais = dadosFuncionario(salvo);
        if (dadosAnteriores == null) {
            historico.registrar("FUNCIONARIO", salvo.getId(), "CRIACAO", null, dadosAtuais);
        } else {
            historico.registrar("FUNCIONARIO", salvo.getId(), "EDICAO", dadosAnteriores, dadosAtuais);
        }

        if (!anterior.isEmpty() && !anterior.getFirst().equals(salvo.getStatus())) {
            boolean retorno = "At".equals(salvo.getStatus());
            var depois = new java.util.LinkedHashMap<String, Object>();
            depois.put("status", salvo.getStatus());
            if (retorno) depois.put("observacoes", OBSERVACOES_RETORNO);
            historico.registrar("FUNCIONARIO", salvo.getId(), retorno ? "REATIVACAO" : "MUDANCA_STATUS",
                    java.util.Map.of("status", anterior.getFirst()), depois);
        }
        return salvo;
    }

    private java.util.Map<String, Object> dadosFuncionario(Funcionario funcionario) {
        var dados = new java.util.LinkedHashMap<String, Object>();
        dados.put("nome", funcionario.getNome());
        dados.put("cpf", funcionario.getCpf());
        dados.put("email", funcionario.getEmail());
        dados.put("cargo", funcionario.getCargo());
        dados.put("setor", funcionario.getSetor());
        dados.put("permissoes", funcionario.getPermissoes());
        dados.put("nRs", funcionario.getnRs().stream().sorted().toList());
        return dados;
    }

    private java.util.Map<String, Object> dadosPersistidos(Long id) {
        var linhas = jdbc.queryForList("select nome, cpf, email, cargo, setor, permissoes from funcionarios where id=?", id);
        if (linhas.isEmpty()) throw new RegistroNaoEncontradoException("Funcionário não encontrado.");
        var origem = linhas.getFirst();
        var dados = new java.util.LinkedHashMap<String, Object>();
        for (var campo : List.of("nome", "cpf", "email", "cargo", "setor", "permissoes")) {
            dados.put(campo, origem.get(campo));
        }
        dados.put("nRs", jdbc.queryForList(
                "select n.nome from funcionarios_nrs fn join nrs n on n.id=fn.nr_id where fn.funcionario_id=?",
                String.class, id).stream().sorted().toList());
        return dados;
    }

    /**
     * Atualiza somente os campos informados.
     */
    @Transactional
    public Funcionario atualizarParcial(Long id, FuncionarioPatchRequest request) {
        Funcionario funcionario = buscar(id);

        if (request.email() != null) funcionario.setEmail(request.email());
        if (request.nome() != null) {
            funcionario.setNome(request.nome());
        }
        if (request.cargo() != null) {
            funcionario.setCargo(request.cargo());
        }
        if (request.cpf() != null) {
            funcionario.setCpf(request.cpf());
        }
        if (request.setor() != null) {
            funcionario.setSetor(request.setor());
        }
        if (request.permissoes() != null) {
            br.com.tst.security.AccountPolicy.checkRole(funcionario.getPermissoes(),request.permissoes());
            funcionario.setPermissoes(request.permissoes());
        }
        if (request.status() != null) {
            funcionario.setStatus(request.status());
        }
        if (request.nRs() != null) {
            funcionario.setnRs(request.nRs());
        }

        return salvar(funcionario);
    }

    /**
     * Exclui um funcionário.
     */
    @Transactional
    public void excluir(Long id) {

        // Garante que o funcionário realmente existe.
        buscar(id);

        // Funcionários nunca são apagados: o ID permanece e o status passa a Inativo.
        var funcionario = buscar(id);
        var antes = funcionario.getStatus();
        if (!"In".equals(antes)) {
            funcionario.setStatus("In");
            funcionarioRepository.save(funcionario);
            historico.registrar("FUNCIONARIO", id, "INATIVACAO", java.util.Map.of("status", antes), java.util.Map.of("status", "In"));
        }
    }
}