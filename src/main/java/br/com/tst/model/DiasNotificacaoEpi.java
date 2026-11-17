package br.com.tst.model;
import jakarta.persistence.*;
@Entity @Table(name="dias_notificacao_epis")
public class DiasNotificacaoEpi {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private Integer ca;
 @Column(nullable=false) private Integer diasAviso;
 public Long getId(){return id;} public Integer getCa(){return ca;} public Integer getDiasAviso(){return diasAviso;}
 public void setCa(Integer v){ca=v;} public void setDiasAviso(Integer v){diasAviso=v;}
}
