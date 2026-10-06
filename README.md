# Backend TST

API Spring Boot com persistência PostgreSQL.

## Configuração do banco

Configure estas variáveis de ambiente no serviço da aplicação:

- `SPRING_DATASOURCE_URL`: URL JDBC completa, por exemplo
  `jdbc:postgresql://HOST:PORT/NOME_DO_BANCO`.
- `SPRING_DATASOURCE_USERNAME`: usuário do banco.
- `SPRING_DATASOURCE_PASSWORD`: senha do banco.

No Render, use o host e a porta fornecidos pelo provedor do banco e confirme que
ele aceita conexões a partir do serviço da aplicação. O banco precisa estar
ativo e acessível pela rede; `localhost` aponta para o próprio container da
aplicação, não para um banco separado. Configure essas variáveis também no ambiente local para apontar para
uma instância PostgreSQL acessível. `DB_USERNAME` continua aceito como
alternativa para o nome de usuário.

Defina `SPRING_DATASOURCE_PASSWORD` com a senha atual do banco, sem colocá-la na
URL JDBC e sem codificá-la como URL. Se uma senha anterior foi adicionada ao
repositório, altere-a no provedor e atualize o segredo no Render.