# Serviço Já — API

API REST do **Serviço Já**, plataforma que conecta clientes a empresas de serviços. Este repositório contém apenas o **backend** (Spring Boot + PostgreSQL), preparado para ser consumido por um futuro aplicativo mobile (Flutter).

## Tecnologias

- Java 21
- Spring Boot 4.1.0
- Spring Data JPA + Hibernate
- Spring Security + JWT (jjwt 0.13.0)
- Flyway (migrações de banco)
- PostgreSQL
- Springdoc OpenAPI (Swagger UI)
- Lombok

## Requisitos

- JDK 21 ou superior
- Maven 3.9+ (ou use o wrapper `./mvnw`, que não exige Maven instalado)
- PostgreSQL 17+ (com um banco chamado `servico_ja` criado)

## Como rodar

### Opção 1 — Docker Compose (recomendado)

Sobe a API e o Postgres juntos, sem precisar instalar Java/Maven/Postgres localmente:

```bash
cp .env.example .env   # na raiz do projeto; ajuste os valores se quiser
docker compose up --build
```

A API fica disponível em http://localhost:8080. Os dados do Postgres e os arquivos
enviados (fotos/logo) ficam em volumes Docker nomeados (`db-data`, `uploads-data`),
então sobrevivem a `docker compose down` (mas não a `docker compose down -v`).

### Opção 2 — local com Maven

1. Crie o banco de dados (usuário `servico_ja`, senha `servico_ja`):

   ```sql
   CREATE USER servico_ja WITH PASSWORD 'servico_ja';
   CREATE DATABASE servico_ja OWNER servico_ja;
   ```

2. Execute a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```

3. Acesse:

   - Swagger UI: http://localhost:8080/swagger-ui.html
   - OpenAPI JSON: http://localhost:8080/v3/api-docs
   - Health check: http://localhost:8080/actuator/health

   O Swagger e o OpenAPI ficam desligados com `SPRING_PROFILES_ACTIVE=prod`.

## Configuração (variáveis de ambiente)

| Variável | Padrão | Descrição |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/servico_ja` | URL do banco |
| `DB_USER` | `servico_ja` | Usuário do banco |
| `DB_PASSWORD` | `servico_ja` | Senha do banco |
| `JWT_SECRET` | valor de desenvolvimento | Segredo para assinar os JWT (mín. 32 bytes) |
| `JWT_EXPIRACAO_ACESSO_MIN` | `60` | Validade do token de acesso em minutos |
| `JWT_EXPIRACAO_REFRESH_DIAS` | `30` | Validade do refresh token em dias |
| `ASAAS_URL` | `https://api-sandbox.asaas.com` | URL da API do Asaas (sem `/v3`). Produção: `https://api.asaas.com` |
| `ASAAS_API_KEY` | vazio | Chave de API do Asaas (vazia = pagamentos desativados) |
| `ASAAS_WEBHOOK_TOKEN` | vazio | Token de autenticação do webhook, o mesmo cadastrado no painel do Asaas (32 a 255 caracteres). Obrigatório em `prod` quando `ASAAS_API_KEY` está definida |
| `ASAAS_VALOR_MENSAL` | `49.90` | Valor mensal da assinatura Premium |
| `ASAAS_VALOR_ANUAL` | `479.00` | Valor anual da assinatura Premium |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USER` / `MAIL_PASSWORD` | Gmail SMTP | Envio de e-mails |
| `APP_BASE_URL` | `http://localhost:8080` | URL pública da API, usada para montar as URLs das fotos/logo enviados |
| `CIDADE_PADRAO` | `Marau` | Cidade padrão dos novos usuários |
| `UF_PADRAO` | `RS` | UF padrão dos novos usuários |
| `FUSO_HORARIO` | `America/Sao_Paulo` | Fuso usado para calcular se a empresa está "aberta agora" |
| `PORT` | `8080` | Porta do servidor |
| `UPLOAD_DIR` | `./uploads` | Diretório onde fotos/logo enviados pelos usuários são gravados |
| `CORS_ALLOWED_ORIGINS` | `*` (vazio em `prod`) | Origens web permitidas por CORS, separadas por vírgula. O app mobile não precisa de CORS; em produção só defina se houver um cliente web (ex.: `https://admin.servicoja.com.br`) |
| `ADMIN_SEED_EMAIL` | `admin@servicoja.com.br` | E-mail do administrador inicial |
| `ADMIN_SEED_SENHA` | vazio | Senha do administrador inicial. Obrigatória em `prod` (mín. 12 caracteres); fora de `prod`, se vazia, uma senha aleatória é gerada e exibida no log |

## Dados iniciais

- **Categorias**: criadas pela migração `V3__categorias_iniciais.sql`, em qualquer ambiente.
- **Administrador**: criado na subida da aplicação enquanto não existir nenhum usuário `ADMIN`.
  - Em `prod`, a senha precisa vir de `ADMIN_SEED_SENHA` (mínimo 12 caracteres) e nunca é
    gerada nem escrita no log. Sem ela, a aplicação sobe normalmente, mas avisa no log que
    nenhum administrador foi criado. Depois do primeiro login, troque a senha pelo app e
    remova `ADMIN_SEED_SENHA` do ambiente.
  - Fora de `prod`, se `ADMIN_SEED_SENHA` não for definida, uma senha aleatória é gerada e
    aparece uma única vez no log:

    ```
    Administrador inicial criado (admin@servicoja.com.br) com senha gerada automaticamente: xxxxxxxxxxxxxxxx
    ```
- **Empresas de exemplo** (senha `senha123`): criadas apenas fora de `prod`, com localização em
  Marau e horários de funcionamento (a Hidráulica Marau atende 24 horas, útil para testar "aberto agora").

## Endpoints principais

### Autenticação (`/api/auth`)
| Método | Rota | Descrição |
| --- | --- | --- |
| POST | `/api/auth/cadastro/cliente` | Cadastro de cliente |
| POST | `/api/auth/cadastro/empresa` | Cadastro de empresa |
| POST | `/api/auth/login` | Login (devolve access + refresh token) |
| POST | `/api/auth/refresh` | Renova o access token |
| POST | `/api/auth/logout` | Invalida o refresh token |
| POST | `/api/auth/recuperar-senha` | Solicita recuperação de senha |
| POST | `/api/auth/redefinir-senha` | Redefine a senha com o token |
| PUT | `/api/auth/senha` | Altera a própria senha |
| PUT | `/api/auth/perfil` | Atualiza dados do usuário logado |
| GET | `/api/auth/me` | Dados do usuário logado |
| POST | `/api/auth/excluir-conta` | Exclui a própria conta (exige a senha atual; indisponível para `ADMIN`) |

### Categorias (`/api/categorias`)
| Método | Rota | Descrição |
| --- | --- | --- |
| GET | `/api/categorias` | Lista categorias ativas |
| GET | `/api/categorias/todas` | Lista todas as categorias (admin) |
| POST | `/api/categorias` | Cria categoria (admin) |
| PUT | `/api/categorias/{id}` | Atualiza categoria (admin) |
| PATCH | `/api/categorias/{id}/atividade` | Ativa/desativa categoria (admin) |
| DELETE | `/api/categorias/{id}` | Remove categoria (admin) |

### Empresas (`/api/empresas`)
| Método | Rota | Descrição |
| --- | --- | --- |
| GET | `/api/empresas` | Busca com filtros (categoria, nome, cidade, UF) e paginação. Com `latitude`/`longitude`, ordena por proximidade e devolve `distanciaKm`; com `abertas=true`, só traz quem está aberto agora |
| GET | `/api/empresas/{id}` | Perfil público da empresa (inclui `horarios` e `abertoAgora`) |
| POST | `/api/empresas/{id}/eventos` | Registra visualização ou clique em WhatsApp/ligar/mapa (público; 1 por pessoa a cada 30 min, o dono não conta) |
| GET | `/api/empresas/{id}/desempenho` | Painel do dono: visualizações e cliques nos últimos 30 dias, comparados aos 30 anteriores |
| GET | `/api/empresas/minhas` | Empresas do usuário logado |
| POST | `/api/empresas` | Cadastra empresa (dono) |
| PUT | `/api/empresas/{id}` | Atualiza empresa (dono). O logo não faz parte deste corpo. `horarios` é uma lista de `{diaSemana (1=segunda..7=domingo), abre, fecha}` em `HH:mm`. Mudar nome, categoria, descrições, contatos, site ou redes faz a empresa voltar para análise; horários, endereço e localização não |
| DELETE | `/api/empresas/{id}` | Exclui empresa (dono): cancela o Premium no Asaas e tira a empresa da plataforma |
| POST | `/api/empresas/{id}/fotos` | Envia uma foto (multipart/form-data, exige Premium) |
| DELETE | `/api/empresas/{id}/fotos/{fotoId}` | Remove foto do portfolio |
| POST | `/api/empresas/{id}/logo` | Envia o logo da empresa (multipart/form-data) |
| POST | `/api/empresas/{id}/portfolios` | Adiciona item ao portfolio |
| DELETE | `/api/empresas/{id}/portfolios/{portfolioId}` | Remove item do portfolio |
| POST | `/api/empresas/{id}/destaque` | Ativa destaque (exige Premium) |
| DELETE | `/api/empresas/{id}/destaque` | Remove destaque |

### Avaliações (`/api/avaliacoes`)
| Método | Rota | Descrição |
| --- | --- | --- |
| GET | `/api/avaliacoes/empresas/{empresaId}` | Avaliações aprovadas de uma empresa |
| POST | `/api/avaliacoes/empresas/{empresaId}` | Avalia uma empresa (cliente) |
| GET | `/api/avaliacoes/minhas` | Avaliações do usuário logado |

### Favoritos (`/api/favoritos`)
| Método | Rota | Descrição |
| --- | --- | --- |
| GET | `/api/favoritos` | Lista favoritos do usuário |
| POST | `/api/favoritos/empresas/{empresaId}` | Favorita uma empresa |
| DELETE | `/api/favoritos/empresas/{empresaId}` | Desfavorita uma empresa |
| GET | `/api/favoritos/estado` | Indica se empresas estão favoritadas |

### Assinaturas (`/api/assinaturas`)
| Método | Rota | Descrição |
| --- | --- | --- |
| POST | `/api/assinaturas` | Cria assinatura Premium (gera pagamento no Asaas) |
| GET | `/api/assinaturas/empresas/{empresaId}` | Assinatura da empresa |
| DELETE | `/api/assinaturas/{id}` | Cancela a assinatura |

### Asaas (`/api/asaas`)
| Método | Rota | Descrição |
| --- | --- | --- |
| POST | `/api/asaas/webhook` | Recebe eventos de pagamento do Asaas |

### Notificações (`/api/notificacoes`)
| Método | Rota | Descrição |
| --- | --- | --- |
| GET | `/api/notificacoes` | Lista notificações do usuário |
| GET | `/api/notificacoes/nao-lidas` | Conta notificações não lidas |
| PATCH | `/api/notificacoes/{id}/lida` | Marca como lida |
| PATCH | `/api/notificacoes/lidas` | Marca todas como lidas |

### Administração (`/api/admin`)
| Método | Rota | Descrição |
| --- | --- | --- |
| GET | `/api/admin/estatisticas` | Estatísticas gerais |
| GET | `/api/admin/empresas` | Lista empresas (pendentes e aprovadas) |
| PATCH | `/api/admin/empresas/{id}/aprovacao` | Aprova/rejeita empresa |
| GET | `/api/admin/avaliacoes/pendentes` | Avaliações aguardando moderação |
| PATCH | `/api/admin/avaliacoes/{id}/moderacao` | Aprova/rejeita avaliação |
| GET | `/api/admin/usuarios` | Lista usuários |
| GET | `/api/admin/logs` | Lista logs do sistema |

## Upload de arquivos (fotos e logo)

`POST /api/empresas/{id}/fotos` e `POST /api/empresas/{id}/logo` recebem
`multipart/form-data` com um campo `arquivo` (imagem JPG/PNG/WEBP, até 5MB); o endpoint
de fotos aceita ainda `descricao` e `ordem` como campos de texto adicionais. O formato é
identificado pelo conteúdo do arquivo (assinatura binária), não pelo nome nem pelo
`Content-Type` enviados, e o arquivo é salvo com um nome aleatório. O logo só pode ser
alterado por esse upload.

Os arquivos são gravados em disco, em `${UPLOAD_DIR}/empresas/{id}/fotos|logo/`, e
servidos publicamente em `/uploads/**`. A gravação em disco é uma implementação da
interface `ArmazenamentoArquivos` (`infra/armazenamento`) — trocar para um bucket
(S3, Cloudinary etc.) no futuro é criar uma nova implementação dessa interface, sem
mudar controllers/services. Em produção com múltiplas instâncias/containers efêmeros,
`UPLOAD_DIR` deve apontar para um volume persistente compartilhado (ou a interface deve
ganhar uma implementação baseada em object storage).

## Segurança

- Endpoints públicos: login, registro, listagem de categorias/empresas/avaliações, webhook do Asaas, recuperação de senha e arquivos em `/uploads/**`.
- Demais endpoints exigem token JWT via header `Authorization: Bearer <token>`. Contas desativadas ou excluídas perdem o acesso na hora, mesmo com um token ainda válido.
- Ações de administração (`/api/admin/**` e toda escrita em `/api/categorias`) exigem o perfil `ADMIN`.
- O webhook do Asaas só é processado se o header `asaas-access-token` for igual a `ASAAS_WEBHOOK_TOKEN` (comparação em tempo constante).
- Há um limitador de requisições por IP para login, registro, recuperação de senha e exclusão de conta. Atrás de proxy/load balancer, o IP real vem de `X-Forwarded-For` (`server.forward-headers-strategy=native`), aceito apenas quando a conexão chega de um proxy da rede interna.
- CORS: `CORS_ALLOWED_ORIGINS` (padrão `*` fora de `prod`; em `prod`, nenhuma origem por padrão). A API não usa cookies (`allowCredentials=false`).
- Exclusão de conta (LGPD): apaga favoritos, avaliações, notificações e tokens, remove as empresas do usuário (com cancelamento do Premium) e anonimiza o cadastro. Assinaturas, pagamentos e logs de acesso são mantidos por obrigação legal, sem dados pessoais.
- `/actuator/health` é o único endpoint do Actuator exposto (`management.endpoints.web.exposure.include=health`), sem detalhes sensíveis (`show-details: never`).

## Testes

Os testes de integração usam um banco Postgres real (`servico_ja_teste`) — não usam
Testcontainers nem H2. Antes de rodar, crie o banco:

```bash
CREATE DATABASE servico_ja_teste OWNER servico_ja;
```

Depois:

```bash
./mvnw test
```

Se preferir não instalar Postgres localmente, suba um container temporário:

```bash
docker run -d --name pg-teste -e POSTGRES_USER=servico_ja -e POSTGRES_PASSWORD=servico_ja \
  -e POSTGRES_DB=servico_ja_teste -p 5432:5432 postgres:17-alpine
./mvnw test
docker rm -f pg-teste
```

## Docker

```bash
docker build -t servicoja-backend .
docker run --rm -p 8080:8080 --env-file ../.env servicoja-backend
```

Para subir a API junto com o Postgres, use o `docker-compose.yml` na raiz do
repositório (veja a seção "Como rodar" acima).

## CI

O workflow `.github/workflows/backend-ci.yml` roda `./mvnw test` (com um Postgres de
serviço) e valida o build da imagem Docker a cada push/PR que toque em `backend/**`.

## Checklist antes de ir para produção

- [ ] `SPRING_PROFILES_ACTIVE=prod` (exige um `JWT_SECRET` forte, desliga o Swagger e
      as empresas de exemplo, e fecha o CORS)
- [ ] `JWT_SECRET` forte e único (ex.: `openssl rand -base64 48`). A aplicação recusa
      subir em `prod` com o valor de desenvolvimento
- [ ] `ADMIN_SEED_EMAIL`/`ADMIN_SEED_SENHA` (mín. 12 caracteres) para criar o primeiro
      administrador; depois do primeiro login, troque a senha e remova a variável
- [ ] `DB_URL`/`DB_USER`/`DB_PASSWORD` apontando para o Postgres de produção, com senha
      forte e sem porta exposta para a internet
- [ ] `ASAAS_URL=https://api.asaas.com`, `ASAAS_API_KEY` de produção e
      `ASAAS_WEBHOOK_TOKEN` (ex.: `openssl rand -hex 32`), cadastrando o mesmo token no
      webhook do painel do Asaas apontando para `https://<sua-api>/api/asaas/webhook`
- [ ] `APP_BASE_URL` com a URL pública (HTTPS) da API
- [ ] `MAIL_*` configurado com uma conta/serviço SMTP real
- [ ] `UPLOAD_DIR` apontando para um volume persistente (backup incluído)
- [ ] HTTPS/domínio configurados na camada de proxy/load balancer na frente da API
- [ ] Backup automático do Postgres

## Estrutura do projeto

```
backend/
├── pom.xml
└── src
    ├── main
    │   ├── java/com/servicoja
    │   │   ├── api/          # Controllers e DTOs (auth, admin, categorias, empresas...)
    │   │   ├── dominio/      # Entidades, repositórios e enums
    │   │   ├── infra/        # Exceções, configurações e serviços transversais
    │   │   ├── pagamento/    # Integração com o Asaas
    │   │   ├── seguranca/    # JWT e configuração do Spring Security
    │   │   └── ServicoJaApiApplication.java
    │   └── resources
    │       ├── application.yml
    │       ├── application-prod.yml # Ajustes aplicados só em produção
    │       └── db/migration/ # Migrações Flyway
    └── test                  # Testes de integração
```

## Fluxo de dados do MVP

1. Cliente se cadastra e faz login (JWT).
2. Empresa se cadastra e aguarda aprovação do administrador.
3. Empresa aprovada aparece na busca pública (categoria, cidade, avaliação).
4. Cliente avalia e favorita empresas.
5. Empresa assina o Premium via Asaas; o pagamento é confirmado pelo webhook e a assinatura é ativada.
6. Cliente recebe notificações de novas avaliações/empresas e o admin modera o conteúdo.
