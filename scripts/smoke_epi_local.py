"""Uses only the disposable PostgreSQL fixture and API on localhost:18082."""
import json,urllib.request,urllib.error,concurrent.futures
from datetime import datetime
from zoneinfo import ZoneInfo
BASE='http://localhost:18082';today=datetime.now(ZoneInfo('America/Sao_Paulo')).date().isoformat()
def call(path,method='GET',data=None,token=None):
 req=urllib.request.Request(BASE+path,method=method,data=None if data is None else json.dumps(data).encode(),headers={'Content-Type':'application/json',**({'Authorization':'Bearer '+token}if token else {})})
 try:
  with urllib.request.urlopen(req)as r:return r.status,json.loads(r.read()or b'null')
 except urllib.error.HTTPError as e:return e.code,e.read().decode()
code,auth=call('/auth/login','POST',{'email':'12345678901','senha':'Local-only-test-123'});assert code==200,(code,auth);token=auth['access_token']
base={'ca':12345,'nome':'Capacete teste','lote':'LOCAL','vencimento':'2030-01-01','substituido':False,'funcionarioIds':[]}
code,res=call('/epis/bulk','POST',{'epis':[base]*6},token);assert code==201,(code,res);ids=[x['id']for x in res['items']];assert len(set(ids))==6
code,suggestions=call('/epis/sugestoes?ca=123',token=token);assert code==200 and len([x for x in suggestions if x['nome']=='Capacete teste'])==1
code,delivery=call('/entregas-epis','POST',{'funcionarioId':2,'dataEntrega':today,'epiIds':ids[:2]},token);assert code==201,(code,delivery);assert delivery['responsavelId']==1
assert call('/entregas-epis','POST',{'funcionarioId':1,'dataEntrega':today,'epiIds':[ids[0]]},token)[0]==409
code,replacement=call('/epis/'+str(ids[0])+'/substituicao','POST',{'novoEpiId':ids[2],'motivo':'Teste substituição','data':today},token);assert code==200,(code,replacement)
old=call('/epis/'+str(ids[0]),token=token)[1];assert old['substituido']and not old['funcionarios']
new=call('/epis/'+str(ids[2]),token=token)[1];assert new['funcionarios'][0]['id']==2
# Entire delivery must roll back when one object in the batch is already assigned.
assert call('/entregas-epis','POST',{'funcionarioId':2,'dataEntrega':today,'epiIds':[ids[3],ids[2]]},token)[0]==409
assert not call('/epis/'+str(ids[3]),token=token)[1]['funcionarios']
assert call('/epis/'+str(ids[2])+'/descarte','POST',{'motivo':'Teste descarte','data':today},token)[0]==200
discard=call('/epis/'+str(ids[2]),token=token)[1];assert discard['dataDescarte'] and discard['motivoDescarte']=='Teste descarte'and discard['responsavelDescarteId']==1 and not discard['funcionarios']
assert call('/epis/'+str(ids[2])+'/descarte','POST',{'motivo':'De novo','data':today},token)[0]==409
assert call('/epis/'+str(ids[2]),'DELETE',None,token)[0]==405
# Concurrent deliveries of one object cannot both win.
def race(person):return call('/entregas-epis','POST',{'funcionarioId':person,'dataEntrega':today,'epiIds':[ids[4]]},token)[0]
with concurrent.futures.ThreadPoolExecutor(2)as pool:assert sorted(pool.map(race,[1,2]))==[201,409]
assert call('/epis/'+str(ids[1])+'/substituicao','POST',{'novoEpiId':ids[1],'motivo':'Mesmo','data':today},token)[0]==409
assert call('/epis/'+str(ids[1])+'/descarte','POST',{'motivo':'','data':today},token)[0]==400
# ADMIN creates worker test credential; worker can neither deliver nor discard.
assert call('/funcionarios/2/senha','POST',{'senha':'Local-worker-123'},token)[0]==200
_,worker=call('/auth/login','POST',{'email':'12345678902','senha':'Local-worker-123'});w=worker['access_token']
assert call('/entregas-epis',token=w)[0]==403
assert call('/epis/'+str(ids[1])+'/descarte','POST',{'motivo':'Tentativa','data':today},w)[0]==403
code,own=call('/portal/epis',token=w);assert code==200 and ids[2]not in [x['id']for x in own]
assert call('/auth/logout','POST',{},w)[0]==200
# Inactive employees cannot receive a replacement; the old assignment stays intact.
assert call('/funcionarios/2','PATCH',{'status':'In'},token)[0]==200
assert call('/epis/'+str(ids[1])+'/substituicao','POST',{'novoEpiId':ids[5],'motivo':'Inativo','data':today},token)[0]==409
assert not call('/epis/'+str(ids[1]),token=token)[1]['substituido']
assert not call('/epis/'+str(ids[5]),token=token)[1]['funcionarios']
assert call('/funcionarios/2','PATCH',{'status':'At'},token)[0]==200
assert call('/auth/logout','POST',{},token)[0]==200
print('PASS: physical bulk, CA suggestions, canonical delivery, replacement, discard/audit, rollback, duplicate/concurrent assignment, ownership and role restrictions.')
