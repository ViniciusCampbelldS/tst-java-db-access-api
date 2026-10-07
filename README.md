# Backend TST

API Spring Boot com persistência PostgreSQL.

## Configuração do banco

Configure estas variáveis de ambiente no serviço da aplicação. Para o Session
Pooler do Supabase, se o painel mostrar `postgres.<PROJECT_REF>` como usuário:

```properties
SPRING_DATASOURCE_URL=:jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres.aabhyazfeeiwegchfxrl
SPRING_DATASOURCE_PASSWORD=<senha atual do banco>
```

Como URL, usuário e senha são configurados separadamente pelo Spring (em application.properties), não
acrescente `user=` ou `password=` à URL JDBC. Informe a senha atual sem
codificação de URL (%20 por exemplo) na variável `SPRING_DATASOURCE_PASSWORD`.

No Render, use o host (aws-0-us-east-1.pooler.supabase.com) e a porta (5432) fornecidos pelo provedor do
banco e confirme que ele aceita conexões a partir do serviço da aplicação. Para Supabase no Render,
prefira o **Session pooler** porque a conexão direta não é acessível por
IPv4.

Configure as mesmas variáveis no ambiente local (do Render) para apontar a uma instância
PostgreSQL acessível. Mantenha a senha apenas em `SPRING_DATASOURCE_PASSWORD`,
não na URL nem no repositório. Se uma senha já foi compartilhada ou commitada,
troque-a no provedor e atualize o segredo no Render.

Para montar `SPRING_DATASOURCE_URL`, use os dados de conexão PostgreSQL em
**Connect** no Supabase, convertendo o prefixo `postgresql://` --> para --> `jdbc:postgresql://`.

OBS.:

O erro `Network is unreachable` indica que o Render não
alcança o host/porta configurados; o erro posterior de dialeto do Hibernate é
uma consequência da conexão indisponível.

Não utilize as variáveis `SUPABASE_URL`, `SUPABASE_PUBLISHABLE_KEY`,
`SUPABASE_SECRET_KEY` e `SUPABASE_JWKS_URL` que são para APIs Supabase e
autenticação.
`npm install @supabase/server` também não se aplica a este backend Java.