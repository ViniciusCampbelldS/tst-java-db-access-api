# Loja Senac — Spring Boot, JPA e MySQL

Projeto didático completo para demonstrar ORM com Java, Spring Data JPA,
Hibernate, Thymeleaf, Bootstrap e MySQL.

## Funcionalidades

- CRUD de categorias;
- CRUD de produtos;
- relacionamento `@ManyToMany` entre produtos e categorias;
- validação dos formulários;
- mensagens de sucesso e erro;
- bloqueio da exclusão de categorias que possuem produtos;
- carregamento inicial das categorias Informática e Escritório;
- interface responsiva com Bootstrap.

## Requisitos

- Java 17 ou superior;
- IntelliJ IDEA;
- Maven 3.9 ou Maven integrado ao IntelliJ;
- MySQL 8 ou Docker Desktop.

## Opção 1 — executar o MySQL com Docker

Na pasta do projeto, execute:

```bash
docker compose up -d
```

O container criará automaticamente:

- banco: `loja_senac`;
- usuário: `root`;
- senha: `root`;
- porta: `3306`.

A URL JDBC padrão cria automaticamente o banco `loja_senac` quando ele ainda
não existe. Para conectar ao container, informe `DB_PASSWORD=root` nas
variáveis de ambiente da aplicação.

## Opção 2 — utilizar o MySQL instalado

Abra o arquivo `banco.sql` no MySQL Workbench e execute o script.

Depois, informe sua senha em `src/main/resources/application.properties` ou
crie a variável de ambiente `DB_PASSWORD`.

Configuração padrão:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/loja_senac
spring.datasource.username=root
spring.datasource.password=root
```

O projeto aceita estas variáveis de ambiente:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Também há endpoints REST para categorias em `/api/categorias`. Para criar ou
atualizar uma categoria, envie JSON com `nome` e `descricao`. A API de produtos
recebe a lista de IDs em `categoriaIds`.

## Abrir no IntelliJ

1. Selecione **File > Open**.
2. Escolha a pasta `loja-senac-mysql`.
3. Aguarde o IntelliJ importar o `pom.xml`.
4. Inicie o MySQL.
5. Execute a classe `LojaApplication`.
6. Acesse `http://localhost:8080`.

## Executar pelo terminal

Com Maven instalado:

```bash
mvn spring-boot:run
```

Para gerar o arquivo JAR:

```bash
mvn clean package
java -jar target/loja-senac-1.0.0.jar
```

## Estrutura

```text
src/main/java/br/com/senac/loja
├── config
├── controller
├── form
├── model
├── repository
├── service
└── LojaApplication.java
```

## Endereços

- Produtos: `http://localhost:8080/produtos`
- Categorias: `http://localhost:8080/categorias`

## Consultas para demonstração

```sql
USE loja_senac;

SHOW TABLES;

SELECT * FROM categorias;
SELECT * FROM produtos;

SELECT
    p.id,
    p.nome,
    p.preco,
    p.quantidade,
    c.nome AS categoria
FROM produtos p
INNER JOIN produto_categorias pc ON pc.produto_id = p.id
INNER JOIN categorias c ON c.id = pc.categoria_id;
```

Para preservar o vínculo de produtos existentes ao atualizar o modelo, depois
que o Hibernate criar a tabela `produto_categorias`, copie os vínculos antigos:

```sql
INSERT INTO produto_categorias (produto_id, categoria_id)
SELECT id, categoria_id
FROM produtos
WHERE categoria_id IS NOT NULL;
```

Em seguida, remova a chave estrangeira antiga de `produtos.categoria_id` e a
coluna, pois ela não é mais usada e pode impedir a inclusão de produtos sem
preencher esse campo. Consulte o nome da chave com `SHOW CREATE TABLE produtos`
e use-o no lugar de `<nome_da_chave>`:

```sql
ALTER TABLE produtos DROP FOREIGN KEY <nome_da_chave>;
ALTER TABLE produtos DROP COLUMN categoria_id;
```

## Observação didática

O Hibernate cria e atualiza as tabelas porque o projeto utiliza:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Em produção, o recomendado é controlar as alterações do banco com ferramentas
de migração, como Flyway ou Liquibase.
