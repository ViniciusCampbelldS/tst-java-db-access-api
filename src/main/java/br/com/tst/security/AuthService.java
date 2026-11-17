package br.com.tst.security;
import br.com.tst.model.Funcionario;
import br.com.tst.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.Instant;
@Service
public class AuthService {
 private final FuncionarioRepository repository; private final PasswordEncoder encoder;private final JavaMailSender mail;
 private final String resetUrl,from;
 @Value("${app.recovery.enabled:false}") private boolean recoveryEnabled;
 public AuthService(FuncionarioRepository r,PasswordEncoder p,JavaMailSender m,@Value("${app.reset-url}") String url,@Value("${app.mail-from}")String from){repository=r;encoder=p;mail=m;resetUrl=url;this.from=from;}
 public record LoginResponse(String access_token,Long funcionarioId,String nome,String permissoes,Instant expiraEm){}
 @Transactional public LoginResponse login(String login,String senha){
 var opt=login.contains("@")?repository.findByEmailIgnoreCase(login.trim()):repository.findByCpf(login.replaceAll("\\D",""));
 var f=opt.orElse(null);
 // Always perform an expensive hash comparison, including unknown/uninitialized accounts.
 String hash=f==null||f.getSenhaHash()==null?dummyHash():f.getSenhaHash();
 boolean matches=encoder.matches(senha,hash);
 if(f==null||f.getSenhaHash()==null||!matches||"In".equals(f.getStatus()))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciais inválidas.");
 String token=TokenService.token();f.setSessaoTokenHash(TokenService.hash(token));f.setSessaoExpiraEm(Instant.now().plusSeconds(28800));repository.save(f);
 return new LoginResponse(token,f.getId(),f.getNome(),f.getPermissoes(),f.getSessaoExpiraEm());
 }
 private String dummy;
 private synchronized String dummyHash(){if(dummy==null)dummy=encoder.encode(TokenService.token());return dummy;}
 @Transactional public void recuperar(String email){
 if(!recoveryEnabled)return;
 repository.findByEmailIgnoreCase(email.trim()).filter(f->!"In".equals(f.getStatus())).ifPresent(f->{
 if(f.getRecuperacaoExpiraEm()!=null && f.getRecuperacaoExpiraEm().isAfter(Instant.now().plusSeconds(840)))return;
 String token=TokenService.token();f.setRecuperacaoTokenHash(TokenService.hash(token));f.setRecuperacaoExpiraEm(Instant.now().plusSeconds(900));repository.save(f);
 var message=new SimpleMailMessage();message.setFrom(from);message.setTo(f.getEmail());message.setSubject("Redefinir senha");message.setText("Para definir sua senha, abra o link abaixo. Ele expira em 15 minutos e pode ser usado uma vez.\n"+resetUrl+"?token="+token);
 mail.send(message);
 });
 }
 @Transactional public void redefinir(String token,String senha){
 var f=repository.findByRecuperacaoTokenHash(TokenService.hash(token)).filter(x->x.getRecuperacaoExpiraEm()!=null&&x.getRecuperacaoExpiraEm().isAfter(Instant.now())&&!"In".equals(x.getStatus())).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Link inválido ou expirado."));
 f.setSenhaHash(encoder.encode(senha));f.setRecuperacaoTokenHash(null);f.setRecuperacaoExpiraEm(null);f.setSessaoTokenHash(null);f.setSessaoExpiraEm(null);repository.save(f);
 }
 @Transactional public void logout(Long id){var f=repository.findById(id).orElseThrow();f.setSessaoTokenHash(null);f.setSessaoExpiraEm(null);repository.save(f);}
}
