package br.com.tst.model;
import jakarta.persistence.*;
@Entity @Table(name="dias_notificacao_treinamentos")
public class DiasNotificacaoTreinamento {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private Integer nr;
 @Column(nullable=false) private Integer diasAviso;
 public Long getId(){return id;} public Integer getNr(){return nr;} public Integer getDiasAviso(){return diasAviso;}
 public void setNr(Integer v){nr=v;} public void setDiasAviso(Integer v){diasAviso=v;}
}
