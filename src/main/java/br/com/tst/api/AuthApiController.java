package br.com.tst.api;
import br.com.tst.security.*;
import br.com.tst.dto.FuncionarioResponse;
import br.com.tst.repository.FuncionarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/auth")
public class AuthApiController {
 @org.springframework.beans.factory.annotation.Value("${app.recovery.enabled:false}") private boolean recoveryEnabled;
 private final AuthService service; private final FuncionarioRepository repository;private final AttemptLimiter limiter;
 public AuthApiController(AuthService s,FuncionarioRepository r,AttemptLimiter l){service=s;repository=r;limiter=l;}
 public record LoginRequest(@NotBlank @Size(max=254)String email,@NotBlank @Size(max=72)String senha){}
 public record RecoveryRequest(@NotBlank @Email @Size(max=254)String email){}
 public record ResetRequest(@NotBlank @Size(max=100)String token,@NotBlank @Size(min=8,max=72)String senha){}
 @PostMapping("/login") public AuthService.LoginResponse login(@Valid @RequestBody LoginRequest r,HttpServletRequest q){limiter.check("login:"+q.getRemoteAddr());return service.login(r.email(),r.senha());}
 @PostMapping("/recuperar-senha") public java.util.Map<String,String> recuperar(@Valid @RequestBody RecoveryRequest r,HttpServletRequest q){limiter.check("reset:"+q.getRemoteAddr());try{service.recuperar(r.email());}catch(org.springframework.mail.MailException ex){org.slf4j.LoggerFactory.getLogger(getClass()).warn("Envio de recuperação indisponível; verificar SMTP.");}return java.util.Map.of("mensagem",recoveryEnabled?"Se houver uma conta disponível, enviaremos as instruções ao e-mail informado.":"Recuperação demonstrativa: nenhum e-mail foi enviado.");}
 @PostMapping("/redefinir-senha") public void redefinir(@Valid @RequestBody ResetRequest r,HttpServletRequest q){limiter.check("token:"+q.getRemoteAddr());service.redefinir(r.token(),r.senha());}
 @GetMapping("/me") public FuncionarioResponse me(Authentication auth){return FuncionarioResponse.from(repository.findById(Long.valueOf(auth.getName())).orElseThrow());}
 @PostMapping("/logout") public void logout(Authentication auth){service.logout(Long.valueOf(auth.getName()));}
}
