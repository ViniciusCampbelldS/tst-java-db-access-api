package br.com.tst.security;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
@Component
public class AttemptLimiter {
 private record Window(long start,int count){}
 private final Map<String,Window> windows=new HashMap<>();
 public synchronized void check(String key){long now=System.currentTimeMillis();windows.entrySet().removeIf(e->now-e.getValue().start()>900000);if(windows.size()>10000)throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
 var w=windows.getOrDefault(key,new Window(now,0));if(w.count()>=10)throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Aguarde antes de tentar novamente.");windows.put(key,new Window(w.start(),w.count()+1));}
}
