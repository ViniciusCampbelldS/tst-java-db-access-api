package br.com.tst.service;
import br.com.tst.model.*;
import br.com.tst.repository.EpiRepository;
import br.com.tst.dto.*;
import br.com.tst.form.EpiForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.function.BiFunction;
@Service
public class EpiService {
 private final EpiRepository repository;private final FuncionarioService funcionarios;private final JdbcTemplate jdbc;
 private final HistoricoService historico;
 private static java.util.Map<String,Object> snap(Epi e){var m=new java.util.LinkedHashMap<String,Object>();m.put("ca",e.getCa());m.put("nome",e.getNome());m.put("lote",e.getLote());m.put("vencimento",String.valueOf(e.getVencimento()));return m;}
 public EpiService(EpiRepository r,FuncionarioService f,JdbcTemplate j,HistoricoService h){repository=r;funcionarios=f;jdbc=j;historico=h;}
 private Epi preencher(Epi e){var ids=jdbc.queryForList("select d.funcionario_id from entrega_epi_itens i join entregas_epi d on d.id=i.entrega_id where i.epi_id=? and i.encerrado_em is null",Long.class,e.getId());e.setFuncionarios(ids.stream().map(funcionarios::buscar).toList());return e;}
 @Transactional(readOnly=true)public List<Epi> listar(){return repository.findAll(org.springframework.data.domain.Sort.by("nome","id")).stream().map(this::preencher).toList();}
 @Transactional(readOnly=true)public Epi buscar(Long id){return preencher(repository.findById(id).orElseThrow(()->new RegistroNaoEncontradoException("EPI não encontrado.")));}
 private void validarVinculo(Epi e,List<Long> ids){var current=e.getId()==null?Set.<Long>of():e.getFuncionarios().stream().map(Funcionario::getId).collect(java.util.stream.Collectors.toSet());if(ids!=null&&!new HashSet<>(ids).equals(current))throw new IllegalArgumentException("Use o registro de entrega para alterar o funcionário do objeto.");}
 private void lock(Long id){if(jdbc.queryForList("select id from epis where id=? for update",Long.class,id).isEmpty())throw new RegistroNaoEncontradoException("EPI não encontrado.");}
 @Transactional public Epi salvar(EpiForm f){if(f.getId()!=null)lock(f.getId());var e=f.getId()==null?new Epi():buscar(f.getId());var antes=f.getId()==null?null:snap(e);validarVinculo(e,f.getFuncionarioIds());if(e.getDataDescarte()!=null)throw new IllegalArgumentException("Objeto descartado não pode ser editado.");if(f.isSubstituido()!=e.isSubstituido())throw new IllegalArgumentException("Use a operação de substituição.");e.setCa(f.getCa());e.setNome(f.getNome().trim());e.setLote(f.getLote().trim());e.setVencimento(f.getVencimento());var salvo=repository.save(e);historico.registrar("EPI",salvo.getId(),antes==null?"CRIACAO":"EDICAO",antes,snap(salvo));return salvo;}
 @Transactional public Epi atualizarParcial(Long id,EpiPatchRequest r){lock(id);var e=buscar(id);var antes=snap(e);validarVinculo(e,r.funcionarioIds());if(e.getDataDescarte()!=null)throw new IllegalArgumentException("Objeto descartado não pode ser editado.");if(r.substituido()!=null&&r.substituido()!=e.isSubstituido())throw new IllegalArgumentException("Use a operação de substituição.");if(r.ca()!=null)e.setCa(r.ca());if(r.nome()!=null)e.setNome(r.nome().trim());if(r.lote()!=null)e.setLote(r.lote().trim());if(r.vencimento()!=null)e.setVencimento(r.vencimento());var salvo=repository.save(e);historico.registrar("EPI",salvo.getId(),"EDICAO",antes,snap(salvo));return salvo;}
 @Transactional public List<Epi> salvarEmLote(EpiCreateRequest r,BiFunction<Long,EpiRequest,EpiForm> converter){return r.epis().stream().map(x->salvar(converter.apply(null,x))).toList();}
 public void excluir(Long id){throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,"Use POST /epis/{id}/descarte com data e motivo; o histórico deve ser preservado.");}
 @Transactional(readOnly=true)public List<Epi> disponiveis(){return listar().stream().filter(e->e.getDataDescarte()==null&&!e.isSubstituido()&&e.getFuncionarios().isEmpty()&&!e.getVencimento().isBefore(java.time.LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")))).toList();}
}
