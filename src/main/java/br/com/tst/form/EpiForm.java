package br.com.tst.form;

import br.com.tst.model.Epi;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EpiForm {

    private Long id;

    @NotBlank(message = "Informe o CA.")
    @Size(max = 15, message = "O CA deve possuir no máximo 15 caracteres.")
    private String ca;

    @NotBlank(message = "Informe o nome do epi.")
    @Size(max = 600, message = "O nome deve possuir no máximo 600 caracteres.")
    private String nome;

    @NotNull(message = "Informe o vencimento.")
    private LocalDate vencimento;

    @NotEmpty(message = "Selecione ao menos uma funcionario.")
    private List<@NotNull @Positive Long> funcionarioIds = new ArrayList<>();

    public EpiForm() {
    }

    public static EpiForm from(Epi epi) {
        EpiForm form = new EpiForm();
        form.setId(epi.getId());
        form.setCa(epi.getCa());
        form.setNome(epi.getNome());
        form.setVencimento(epi.getVencimento());
        form.setFuncionarioIds(
            epi.getFuncionarios().stream().map(funcionario -> funcionario.getId()).toList()
        );
        return form;
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

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    public void setVencimento(LocalDate vencimento) {
        this.vencimento = vencimento;
    }

    public List<Long> getFuncionarioIds() {
        return funcionarioIds;
    }

    public void setFuncionarioIds(List<Long> funcionarioIds) {
        this.funcionarioIds = funcionarioIds;
    }
}
