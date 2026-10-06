package br.com.tst.model;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Check;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Table(name = "funcionarios")
@Check(constraints = "status IN ('At', 'Af', 'In') AND permissoes IN ('ADM', 'field', 'tst')")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe o nome da funcionario.")
    @Size(max = 100, message = "O nome deve possuir no máximo 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String nome;

    @Size(max = 40, message = "O cargo deve possuir no máximo 40 caracteres.")
    @Column(length = 40)
    private String cargo;

    @NotBlank(message = "Informe o CPF.")
    @Pattern(
        regexp = "^(?:\\D*\\d){11}\\D*$",
        message = "O CPF deve possuir exatamente 11 números."
    )
    @Column(nullable = false, length = 11)
    private String cpf;

    @Size(max = 40, message = "O setor deve possuir no máximo 40 caracteres.")
    @Column(length = 40)
    private String setor;

    @NotBlank(message = "Informe as permissões do(a) funcionario.")
    @Pattern(regexp = "ADM|field|tst", message = "As permissões devem ser ADM, field ou tst.")
    @Column(nullable = false, length = 10)
    private String permissoes = "field";

    @NotBlank(message = "Informe o status do(a) funcionario.")
    @Pattern(regexp = "At|Af|In", message = "O status deve ser At, Af ou In.")
    @Column(nullable = false, length = 2)
    private String status = "At";

    @NotNull(message = "Informe a lista de NRs.")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "funcionarios_nrs",
        joinColumns = @JoinColumn(name = "funcionario_id")
    )
    @Column(name = "nr", nullable = false, length = 15)
    private List<@NotBlank(message = "Cada NR deve possuir um valor.")
                  @Size(max = 15, message = "Cada NR deve possuir no máximo 15 caracteres.") String> nRs =
        new ArrayList<>();

    @ManyToMany(mappedBy = "funcionarios")
    private List<Epi> epis = new ArrayList<>();

    public Funcionario() {
    }

    public Funcionario(String nome, String cargo, String cpf, String setor) {
        this.nome = nome;
        this.cargo = cargo;
        this.cpf = cpf;
        this.setor = setor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getPermissoes() {
        return permissoes;
    }

    public void setPermissoes(String permissoes) {
        this.permissoes = permissoes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getnRs() {
        return nRs;
    }

    public void setnRs(List<String> nRs) {
        this.nRs = nRs == null ? new ArrayList<>() : new ArrayList<>(nRs);
    }

    @Transient
    public String getnRsText() {
        return String.join(", ", nRs);
    }

    public void setnRsText(String nRsText) {
        this.nRs = nRsText == null || nRsText.isBlank()
            ? new ArrayList<>()
            : Stream.of(nRsText.split(",", -1))
                .map(String::trim)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Epi> getEpis() {
        return epis;
    }

    public void setEpis(List<Epi> epis) {
        this.epis = epis;
    }
}
