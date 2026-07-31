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

- JDK 21
- Maven 3.9+
- PostgreSQL 17+ (com um banco chamado `servico_ja` criado)

## Como rodar

1. Crie o banco de dados (usuário `servico_ja`, senha `servico_ja`):

   ```sql
   CREATE USER servico_ja WITH PASSWORD 'servico_ja';
   CREATE DATABASE servico_ja OWNER servico_ja;
   ```

2. Execute a aplicação:

   ```bash
   mvn spring-boot:run
   ```

3. Acesse:

   - Swagger UI: http://localhost:8080/swagger-ui.html
   - OpenAPI JSON: http://localhost:8080/v3/api-docs

## Configuração (variáveis de ambiente)

| Variável | Padrão | Descrição |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/servico_ja` | URL do banco |
| `DB_USER` | `servico_ja` | Usuário do banco |
| `DB_PASSWORD` | `servico_ja` | Senha do banco |
| `JWT_SECRET` | valor de desenvolvimento | Segredo para assinar os JWT (mín. 32 bytes) |
| `JWT_EXPIRACAO_ACESSO_MIN` | `60` | Validade do token de acesso em minutos |
| `JWT_EXPIRACAO_REFRESH_DIAS` | `30` | Validade do refresh token em dias |
| `ASAAS_URL` | `https://sandbox.asaas.com` | URL da API do Asaas |
| `ASAAS_API_KEY` | vazio | Chave de API do Asaas |
| `ASAAS_WEBHOOK_SEGREDO` | vazio | Segredo para validar o webhook do Asaas |
| `ASAAS_VALOR_MENSAL` | `49.90` | Valor mensal da assinatura Premium |
| `ASAAS_VALOR_ANUAL` | `479.00` | Valor anual da assinatura Premium |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USER` / `MAIL_PASSWORD` | Gmail SMTP | Envio de e-mails |
| `APP_BASE_URL` | `http://localhost:8080` | URL base usada nos links de e-mail |
| `CIDADE_PADRAO` | `Marau` | Cidade padrão dos novos usuários |
| `UF_PADRAO` | `RS` | UF padrão dos novos usuários |
| `PORT` | `8080` | Porta do servidor |

## Usuário administrador (seed)

Ao iniciar, a aplicação cria automaticamente um administrador e categorias iniciais:

- E-mail: `admin@servicoja.com.br`
- Senha: `admin1234`

> Altere a senha em produção.

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
| GET | `/api/empresas` | Busca com filtros (categoria, nome, cidade, UF) e paginação |
| GET | `/api/empresas/{id}` | Perfil público da empresa |
| GET | `/api/empresas/minhas` | Empresas do usuário logado |
| POST | `/api/empresas` | Cadastra empresa (dono) |
| PUT | `/api/empresas/{id}` | Atualiza empresa (dono) |
| DELETE | `/api/empresas/{id}` | Remove empresa (dono) |
| POST | `/api/empresas/{id}/fotos` | Adiciona foto ao portfolio |
| DELETE | `/api/empresas/{id}/fotos/{fotoId}` | Remove foto do portfolio |
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

## Segurança

- Endpoints públicos: login, registro, listagem de categorias/empresas/avaliações, webhook do Asaas e recuperação de senha.
- Demais endpoints exigem token JWT via header `Authorization: Bearer <token>`.
- Ações de administração exigem o perfil `ADMIN`.
- Há um limitador de requisições para evitar abuso em login e registro.

## Testes

Os testes de integração usam o banco `servico_ja_teste`:

```bash
CREATE DATABASE servico_ja_teste OWNER servico_ja;
mvn test
```

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
