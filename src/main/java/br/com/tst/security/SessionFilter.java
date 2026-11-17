package br.com.tst.security;
import br.com.tst.repository.FuncionarioRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
public class SessionFilter extends OncePerRequestFilter {
 private final FuncionarioRepository repository;
 public SessionFilter(FuncionarioRepository repository){this.repository=repository;}
 protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
 String header=req.getHeader("Authorization");
 if(header!=null && header.startsWith("Bearer ") && header.length()<512){repository.findBySessaoTokenHash(TokenService.hash(header.substring(7))).filter(f->!"In".equals(f.getStatus()) && f.getSessaoExpiraEm()!=null && f.getSessaoExpiraEm().isAfter(Instant.now())).ifPresent(f->{
 var auth=new UsernamePasswordAuthenticationToken(f.getId().toString(),null,List.of(new SimpleGrantedAuthority("ROLE_"+f.getPermissoes())));SecurityContextHolder.getContext().setAuthentication(auth);
 });}
 chain.doFilter(req,res);
 }
}
