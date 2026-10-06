package br.com.tst.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "funcionarios",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_funcionario_nome",
        columnNames = "nome"
    )
)
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

    public List<Epi> getEpis() {
        return epis;
    }

    public void setEpis(List<Epi> epis) {
        this.epis = epis;
    }
}
