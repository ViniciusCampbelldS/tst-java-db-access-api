package br.com.tst.config;
import org.springframework.context.annotation.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import br.com.tst.repository.FuncionarioRepository;
@Configuration
public class BootstrapAccount {
 @Bean CommandLineRunner bootstrap(FuncionarioRepository r,PasswordEncoder p,@Value("${app.bootstrap.cpf:}")String cpf,@Value("${app.bootstrap.email:}")String email,@Value("${app.bootstrap.password:}")String senha){return args->{
 if(cpf.isBlank()||email.isBlank()||senha.isBlank())return;
 if(senha.length()<8||senha.length()>72)throw new IllegalArgumentException("Senha inicial deve ter 8 a 72 caracteres.");
 var f=r.findByCpf(cpf).orElseThrow(()->new IllegalArgumentException("Funcionário inicial não existe."));
 if(f.getSenhaHash()!=null)return;
 if(!"ADM".equals(f.getPermissoes()))throw new IllegalArgumentException("Conta inicial deve ser ADM existente.");
 f.setEmail(email.trim().toLowerCase(java.util.Locale.ROOT));f.setSenhaHash(p.encode(senha));r.save(f);
 };}
}
