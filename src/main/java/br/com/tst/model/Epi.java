package br.com.tst.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "epis")
public class Epi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe o CA.")
    @Size(max = 15, message = "O CA deve possuir no máximo 15 caracteres.")
    @Column(nullable = false, length = 15)
    private String ca;

    @NotBlank(message = "Informe o lote.")
    @Size(max = 20, message = "O lote deve possuir no máximo 20 caracteres.")
    @Column(nullable = false, length = 20)
    private String lote;

    @NotBlank(message = "Informe o nome do epi.")
    @Size(max = 600, message = "O nome deve possuir no máximo 600 caracteres.")
    @Column(nullable = false, length = 600)
    private String nome;

    @NotNull(message = "Informe o vencimento.")
    @Column(nullable = false)
    private LocalDate vencimento;

    @Column(nullable = false)
    private boolean substituido;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "epi_funcionarios",
        joinColumns = @JoinColumn(name = "epi_id"),
        inverseJoinColumns = @JoinColumn(name = "funcionario_id")
    )
    private List<Funcionario> funcionarios = new ArrayList<>();

    public Epi() {
    }

    public Epi(String ca, String lote, String nome, LocalDate vencimento,
               boolean substituido, List<Funcionario> funcionarios) {
        this.ca = ca;
        this.lote = lote;
        this.nome = nome;
        this.vencimento = vencimento;
        this.substituido = substituido;
        this.funcionarios = funcionarios;
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

    public String getCa() {
        return ca;
    }

    public void setCa(String ca) {
        this.ca = ca;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    public void setVencimento(LocalDate vencimento) {
        this.vencimento = vencimento;
    }

    public boolean isSubstituido() {
        return substituido;
    }

    public void setSubstituido(boolean substituido) {
        this.substituido = substituido;
    }

    public List<Funcionario> getFuncionarios() {
        return funcionarios;
    }

    public void setFuncionarios(List<Funcionario> funcionarios) {
        this.funcionarios = funcionarios;
    }
}
