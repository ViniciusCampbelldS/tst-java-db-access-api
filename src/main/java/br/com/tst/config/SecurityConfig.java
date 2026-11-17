package br.com.tst.config;
import br.com.tst.repository.FuncionarioRepository;
import br.com.tst.security.SessionFilter;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
 @Bean SecurityFilterChain security(HttpSecurity http,FuncionarioRepository repository)throws Exception{
 return http.cors(c->{}).csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
 .exceptionHandling(e->e.authenticationEntryPoint((q,r,x)->r.sendError(401)).accessDeniedHandler((q,r,x)->r.sendError(403)))
 .authorizeHttpRequests(a->a
 .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
 .requestMatchers("/auth/login","/auth/recuperar-senha","/auth/redefinir-senha","/error").permitAll()
 .requestMatchers("/auth/me","/auth/logout").authenticated()
 .requestMatchers("/portal/**").authenticated()
 .requestMatchers(HttpMethod.GET,"/funcionarios/me").authenticated()
 .requestMatchers(HttpMethod.GET,"/funcionarios/**").hasAnyRole("ADM","rh","tst","gerencia")
 .requestMatchers(HttpMethod.POST,"/funcionarios/*/senha").hasAnyRole("ADM","gerencia")
 .requestMatchers("/funcionarios/**").hasAnyRole("ADM","rh","gerencia")
 .requestMatchers(HttpMethod.GET,"/entregas-epis/**","/epis/**","/nrs/**","/dias-notificacao-epis/**","/dias-notificacao-treinamentos/**","/treinamentos/**").hasAnyRole("ADM","tst","rh","gerencia")
 .requestMatchers("/entregas-epis/**","/epis/**","/nrs/**","/dias-notificacao-epis/**","/dias-notificacao-treinamentos/**","/treinamentos/**").hasAnyRole("ADM","tst","gerencia")
 .requestMatchers(HttpMethod.GET,"/historico").hasAnyRole("ADM","tst","gerencia")
 .anyRequest().denyAll())
 .addFilterBefore(new SessionFilter(repository),UsernamePasswordAuthenticationFilter.class).build();
 }
}
