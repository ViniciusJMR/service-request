# Service Solicitation


## Como Rodar

###  1. Gerar chaves JWT

Execute os comandos abaixo na raiz do projeto para gerar um par de chaves RSA em formato PEM compatível com a configuração atual:

```bash
mkdir -p src/main/resources/keys
openssl genpkey -algorithm RSA -out src/main/resources/keys/private.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in src/main/resources/keys/private.pem -out src/main/resources/keys/public.pem
```

### 2. Configure o usuário admin inicial nas variáveis de ambiente do docker compose (Opcional)
```
#Configuração padrão
      - APP_ADMIN_NAME=Admin
      - APP_ADMIN_EMAIL=admin@email.com
      - APP_ADMIN_PASSWORD=123456
```


### 3. Suba a aplicação pelo compose

```bash
docker compose up --build
```

###  Endpoints
A Collection de endpoints para se usar no Postman pode ser encontrado na pasta [docs](./docs/Service Request.postman_collection.json)


### Migrações
As migrações estão ocorrendo pelo flyway e estão configuradas para rodar assim que assim que a aplicação for iniciada

## Docker

Esta aplicação sobe com dois containers definidos no `docker-compose.yml`:

* `app`: compila a aplicação Spring Boot usando o `Dockerfile`, expõe a porta `8080` do container na porta `8080` da máquina e usa o profile `prod`.
* `db`: sobe um PostgreSQL 17 Alpine, cria o banco `solicitation`, expõe a porta `5432` e guarda os dados no volume Docker `postgres-data`.

O serviço `app` depende do `db` com `condition: service_healthy`. Isso faz o Compose aguardar o healthcheck do PostgreSQL responder antes de iniciar a aplicação. Os dois serviços ficam na rede bridge `checklist-network`, então a aplicação acessa o banco pelo hostname `db`.

O `Dockerfile` usa build em dois estágios:

* estágio `builder`: usa Gradle com JDK 21 para gerar o jar com `./gradlew bootJar -x test`;
* estágio final: usa apenas o JRE 21 Alpine, copia o jar gerado e executa `java -jar app.jar`.

As chaves JWT são lidas por padrão de `src/main/resources/keys`, mas no Docker Compose elas são montadas em `/app/keys` como volume somente leitura. As variáveis `SECURITY_JWT_PUBLIC_KEY` e `SECURITY_JWT_PRIVATE_KEY` apontam para esses arquivos dentro do container.


## Busca de solicitações para analistas

Endpoint:

```http
GET /api/analyst/solicitations/search
```

Parâmetros:

* `q`: busca textual em `title` e `description`.
* `status`: CSV com um ou mais status. Exemplo: `SUBMITTED,IN_REVIEW`.
* `serviceType`: filtro opcional por tipo de serviço.
* `priority`: filtro opcional por prioridade.
* `state`: filtro opcional para `ADMIN`. Para `ANALYST`, este parâmetro é ignorado e substituído pelos estados configurados no coverage do usuário.
* `dateFrom`, `dateTo`: filtram `createdAt`, em formato ISO-8601. Exemplo: `2026-09-03T00:00:00Z`.
* `page`, `size`: paginação.
* `sort`: campo e direção. Exemplo: `createdAt desc`.

Resposta:

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "total": 0
}
```
