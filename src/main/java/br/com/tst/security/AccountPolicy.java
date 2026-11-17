package br.com.tst.security;
public final class AccountPolicy {
 private AccountPolicy(){}
 public static void checkRole(String previous,String next){var a=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
 boolean admin=a!=null && a.getAuthorities().stream().anyMatch(x->x.getAuthority().equals("ROLE_ADM")||x.getAuthority().equals("ROLE_gerencia"));
 if(!admin && !java.util.Objects.equals(previous==null?"field":previous,next))throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"Somente ADMIN/Gerência altera perfil de acesso.");
 }
}
