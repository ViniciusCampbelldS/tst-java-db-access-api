package br.com.tst.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Entity
@Table(
    name = "dias_notificacao",
    uniqueConstraints = @jakarta.persistence.UniqueConstraint(
        name = "uk_dias_notificacao_ca_ou_nr_is_norma",
        columnNames = {"ca_ou_nr", "is_norma"}
    )
)
public class DiasNotificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe o CA ou número da NR.")
    @Size(max = 15, message = "O CA ou número da NR deve possuir no máximo 15 caracteres.")
    @Column(name = "ca_ou_nr", nullable = false, length = 15)
    private String caOuNr;

    // true indica um número de NR; false, um CA de EPI.
    @Column(name = "is_norma", nullable = false)
    private boolean isNorma;

    @NotNull(message = "Informe a quantidade de dias antes do aviso.")
    @Positive(message = "A quantidade de dias deve ser maior que zero.")
    @Column(nullable = false)
    private Integer diasAviso;

    public DiasNotificacao() {
    }

    public DiasNotificacao(String caOuNr, boolean isNorma, Integer diasAviso) {
        this.caOuNr = caOuNr;
        this.isNorma = isNorma;
        this.diasAviso = diasAviso;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCaOuNr() {
        return caOuNr;
    }

    public void setCaOuNr(String caOuNr) {
        this.caOuNr = caOuNr;
    }

    public boolean isNorma() {
        return isNorma;
    }

    public void setNorma(boolean isNorma) {
        this.isNorma = isNorma;
    }

    public Integer getDiasAviso() {
        return diasAviso;
    }

    public void setDiasAviso(Integer diasAviso) {
        this.diasAviso = diasAviso;
    }
}
