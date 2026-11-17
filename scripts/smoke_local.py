import urllib.request,json,urllib.error
base='http://localhost:18080'
def call(path,method='GET',data=None,token=None):
 r=urllib.request.Request(base+path,method=method,data=json.dumps(data).encode() if data is not None else None,headers={'Content-Type':'application/json',**({'Authorization':'Bearer '+token} if token else {})})
 try:
  with urllib.request.urlopen(r) as res:return res.status,json.loads(res.read() or b'null')
 except urllib.error.HTTPError as e:return e.code,e.read().decode()
status,session=call('/auth/login','POST',{'email':'12345678901','senha':'Local-only-test-123'});assert status==200,(status,session)
token=session['access_token']
assert call('/epis')[0]==401
assert call('/auth/me',token=token)[1]['id']==1
status,epis=call('/epis',token=token);assert status==200,(status,epis);assert isinstance(epis[0]['ca'],int)
status,f=call('/funcionarios/2',token=token);assert status==200 and f['nRs']==['NR 00 - Teste'],(status,f)
payload={'nome':'Operário local','cpf':'12345678902','cargo':'Cargo','setor':'Setor','status':'At','permissoes':'field','nRs':['NR 00 - Teste'],'email':'operario@marmetalurgia.com'}
assert call('/funcionarios/2','PUT',payload,token)[0]==200
assert call('/dias-notificacao-treinamentos',token=token)[1][0]['nr'] in [0,1]
assert call('/auth/recuperar-senha','POST',{'email':'inexistente@marmetalurgia.com'})[0]==200
assert call('/auth/logout','POST',{},token)[0]==200
assert call('/auth/me',token=token)[0]==401
print('PostgreSQL + HTTP: login, ownership identity, numeric CA, NR relation/update, notifications, demo recovery and revocation verified.')
_,session=call('/auth/login','POST',{'email':'12345678901','senha':'Local-only-test-123'});admin=session['access_token']
assert call('/funcionarios/2/senha','POST',{'senha':'Local-worker-only-123'},admin)[0]==200
_,session=call('/auth/login','POST',{'email':'12345678902','senha':'Local-worker-only-123'});worker=session['access_token']
assert call('/funcionarios',token=worker)[0]==403
status,items=call('/portal/epis',token=worker);assert status==200 and [e['id'] for e in items]==[1],(status,items)
assert items[0]['diasAviso']==30
assert call('/portal/treinamentos',token=worker)==(200,[])
assert call('/portal/relatos','POST',{'epiId':1,'situacao':'Desgaste','comentario':'Teste local'},worker)[0]==201
assert call('/portal/relatos','POST',{'epiId':2,'situacao':'Desgaste','comentario':'Teste local'},worker)[0]==403
assert call('/funcionarios/1/senha','POST',{'senha':'Not-allowed-test'},worker)[0]==403
print('Worker portal ownership, warning days, own complaints and password administration verified.')
