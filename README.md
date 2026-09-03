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
* `dateFrom`, `dateTo`: filtram `createdAt`, Exemplo: `2026-09-03`.
* `page`, `size`: paginação.
* `sort`: Ordenação através do campo `createdAt`. Exemplo: `desc`.

