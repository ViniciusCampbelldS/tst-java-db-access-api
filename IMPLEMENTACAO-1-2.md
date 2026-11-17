# Banco, contas e permissões

Implementação com PostgreSQL 17 e Java 24. Banco e frontend precisam ser atualizados juntos.

## Migração

`db/migrations/001_align_notifications_and_accounts.sql` é executada uma única vez, em transação. Ela:

- Converte CA em inteiro e mantém CAs repetidos entre objetos físicos.
- Cria as duas tabelas solicitadas, com id/ca/dias_aviso e id/nr/dias_aviso.
- Une as regras das duas fontes legadas, interrompendo a migração se houver conflito em vez de descartar dados. As tabelas antigas ficam preservadas como backup.
- Corrige a NR de teste 40 para o número 0, preservando o vínculo via ON UPDATE CASCADE, e remove geração automática de IDs de NR.
- Mantém nomes de NR no contrato `nRs` existente do Angular, mas resolve/valida esses nomes no Java e grava a associação em `funcionarios_nrs.nr_id`.
- Cria e-mails fictícios `funcionario.<id>@marmetalurgia.com`, campos de senha/hash de sessão/recuperação na própria tabela funcionarios, índices de unicidade e versão otimista.
- Habilita RLS e retira acesso direto das roles anon/authenticated às tabelas da aplicação. O Java usa conexão PostgreSQL confiável; clientes devem usar sua API, nunca credenciais do banco.

`spring.jpa.hibernate.ddl-auto=validate` impede que versões antigas/mapeamentos incompatíveis alterem o esquema automaticamente. Não reiniciar versões antigas com ddl-auto=update após a migração.

## Acesso

Login aceita CPF ou e-mail. Não há seleção de perfil na tela nem credencial fixa no código.

| Perfil | Ações |
| --- | --- |
| ADM / gerencia | Todas as ações implementadas; definir senha e perfil de funcionários |
| tst | Consultar funcionários para seleção; gerenciar EPIs, NRs e notificações; gerir treinamentos na interface existente |
| rh | Cadastro/dados de funcionários; não elevar perfil de acesso nem definir senhas |
| field | Consultar próprios EPIs/participações e enviar relatos de seus objetos |

Somente ADMIN/Gerência altera perfis e define senhas em `POST /funcionarios/{id}/senha`. RH pode cadastrar funcionário com perfil field; a atribuição de outros perfis é administrativa.

Sessão: token aleatório de 256 bits, apenas hash no banco, validade de 8 horas, uma sessão por funcionário. Logout, redefinição e administração de senha revogam a sessão anterior. A API reconsulta situação/perfil a cada chamada. Controle de versão evita sobrescrever credenciais em gravações concorrentes.

A API do portal resolve posse pela entrega mais recente entre as duas estruturas legadas de entrega; na ausência de entrega usa a junção antiga. Empates privilegiam cabeçalho/itens. Isso é uma compatibilidade temporária; consolidação de entregas e encerramento de posse seguem no TODO. Sem data de encerramento histórica, a aplicação não pode inferir devoluções não registradas.

## Primeiro ADMIN e demais senhas

A ativação solicitada é do funcionário ID 7. A senha inicial é configurada diretamente por hash no banco, fora dos commits. Para uma instalação nova, também há bootstrap opcional para um funcionário ADM existente: BOOTSTRAP_ADMIN_CPF, BOOTSTRAP_ADMIN_EMAIL e BOOTSTRAP_ADMIN_PASSWORD; só inicializa conta sem senha. Remover essas variáveis após o primeiro acesso. Nenhuma senha padrão é criada.

Após entrar como ADMIN/Gerência: editar um funcionário e usar Definir senha. A senha deve ter 8 a 72 caracteres. Não enviar hash de senha em respostas nem registrá-lo no histórico.

## Recuperação demonstrativa

Por decisão do usuário, os e-mails são fictícios e não haverá envio funcional. `RECOVERY_EMAIL_ENABLED=false` por padrão: a tela informa o modo demonstrativo, nenhuma mensagem é enviada e nenhum token é criado. O mecanismo de uso único e expiração de 15 minutos existe e é testado com transporte de e-mail simulado. Para ativação futura: configurar SMTP_HOST/PORT/USERNAME/PASSWORD, MAIL_FROM, RESET_PASSWORD_URL e RECOVERY_EMAIL_ENABLED=true; credenciais somente no ambiente. Sem essa configuração, a recuperação não deve ser apresentada como envio real.

## Verificação

- `mvn test` / `mvn package` em Java 24: login, perfil, logout, revogação, reset de uso único e tokens.
- PostgreSQL local: aplicar `db/tests/legacy_fixture.sql`, depois a migração. Validar preservação de NR00 e regras; nunca aplicar fixture no Supabase.
- `scripts/smoke_local.py` usa somente o servidor/fixture local na porta18080 e senhas fictícias de teste. Confere schema validate, CA numérico, vínculos de NR, portal próprio, relato autorizado e rejeição de acesso a outro objeto.
- Frontend: npm ci e npm run build. Há avisos preexistentes de orçamento de CSS/bundle; não impedem o build.

Limitações: limitador de tentativas é em memória por instância/IP; para múltiplas réplicas usar armazenamento compartilhado. Upload de anexos, CRUD real de treinamentos e consolidação das entregas pertencem às próximas etapas, não a esta entrega.
