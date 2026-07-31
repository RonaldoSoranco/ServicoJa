-- Servico Ja - Esquema inicial (todas as tabelas)
-- Tabelas: usuarios, categorias, empresas, avaliacoes, favoritos, assinaturas,
-- pagamentos, fotos, portfolios, notificacoes, logs, tokens de recuperacao e refresh.

CREATE TABLE usuarios (
    id            BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(120) NOT NULL,
    email         VARCHAR(180) NOT NULL UNIQUE,
    senha         VARCHAR(255) NOT NULL,
    telefone      VARCHAR(20),
    perfil        VARCHAR(20)  NOT NULL CHECK (perfil IN ('CLIENTE', 'EMPRESA', 'ADMIN')),
    asaas_cliente_id VARCHAR(100),
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE tokens_recuperacao (
    id         BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT        NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    token      VARCHAR(255)  NOT NULL UNIQUE,
    expira_em  TIMESTAMPTZ   NOT NULL,
    usado      BOOLEAN       NOT NULL DEFAULT FALSE,
    criado_em  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE tokens_refresh (
    id         BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT       NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expira_em  TIMESTAMPTZ  NOT NULL,
    revogado   BOOLEAN      NOT NULL DEFAULT FALSE,
    criado_em  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE categorias (
    id         BIGSERIAL PRIMARY KEY,
    nome       VARCHAR(80) NOT NULL UNIQUE,
    descricao  TEXT,
    icone      VARCHAR(100),
    ativa      BOOLEAN     NOT NULL DEFAULT TRUE,
    criado_em  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE empresas (
    id                   BIGSERIAL PRIMARY KEY,
    usuario_id           BIGINT         NOT NULL REFERENCES usuarios (id),
    categoria_id         BIGINT         NOT NULL REFERENCES categorias (id),
    nome                 VARCHAR(150)   NOT NULL,
    descricao_curta      VARCHAR(255),
    descricao_completa   TEXT,
    logo_url             VARCHAR(500),
    telefone             VARCHAR(20),
    whatsapp             VARCHAR(20),
    email_contato        VARCHAR(180),
    cep                  VARCHAR(9),
    endereco             VARCHAR(255),
    numero               VARCHAR(10),
    bairro               VARCHAR(100),
    cidade               VARCHAR(100)   NOT NULL,
    uf                   VARCHAR(2)     NOT NULL,
    latitude             DECIMAL(10, 7),
    longitude            DECIMAL(10, 7),
    horario_funcionamento TEXT,
    redes_sociais        TEXT,
    site                 VARCHAR(255),
    premium_ativo        BOOLEAN        NOT NULL DEFAULT FALSE,
    premium_ate          TIMESTAMPTZ,
    destaque             BOOLEAN        NOT NULL DEFAULT FALSE,
    aprovada             BOOLEAN        NOT NULL DEFAULT FALSE,
    media_avaliacoes     DECIMAL(2, 1)  NOT NULL DEFAULT 0,
    total_avaliacoes     INT            NOT NULL DEFAULT 0,
    criado_em            TIMESTAMPTZ    NOT NULL DEFAULT now(),
    atualizado_em        TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_empresas_categoria ON empresas (categoria_id);
CREATE INDEX idx_empresas_cidade     ON empresas (cidade, uf);
CREATE INDEX idx_empresas_nome       ON empresas (lower(nome));

CREATE TABLE avaliacoes (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT       NOT NULL REFERENCES usuarios (id),
    empresa_id  BIGINT       NOT NULL REFERENCES empresas (id),
    nota        INT          NOT NULL CHECK (nota BETWEEN 1 AND 5),
    comentario  TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE' CHECK (status IN ('PENDENTE', 'APROVADA', 'REJEITADA')),
    criado_em   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_avaliacao_usuario_empresa UNIQUE (usuario_id, empresa_id)
);

CREATE INDEX idx_avaliacoes_empresa ON avaliacoes (empresa_id);
CREATE INDEX idx_avaliacoes_status   ON avaliacoes (status);

CREATE TABLE favoritos (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT      NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    empresa_id  BIGINT      NOT NULL REFERENCES empresas (id) ON DELETE CASCADE,
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_favorito_usuario_empresa UNIQUE (usuario_id, empresa_id)
);

CREATE TABLE assinaturas (
    id                    BIGSERIAL PRIMARY KEY,
    usuario_id            BIGINT       NOT NULL REFERENCES usuarios (id),
    empresa_id            BIGINT       NOT NULL REFERENCES empresas (id),
    tipo                  VARCHAR(20)  NOT NULL CHECK (tipo IN ('MENSAL', 'ANUAL')),
    status                VARCHAR(20)  NOT NULL DEFAULT 'ATIVA' CHECK (status IN ('ATIVA', 'CANCELADA', 'EXPIRADA')),
    inicio_em             TIMESTAMPTZ,
    fim_em                TIMESTAMPTZ,
    asaas_assinatura_id   VARCHAR(100),
    criado_em             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE pagamentos (
    id                    BIGSERIAL PRIMARY KEY,
    assinatura_id         BIGINT       REFERENCES assinaturas (id),
    empresa_id            BIGINT       NOT NULL REFERENCES empresas (id),
    asaas_pagamento_id    VARCHAR(100),
    valor                 DECIMAL(10, 2) NOT NULL,
    status                VARCHAR(30)  NOT NULL DEFAULT 'PENDENTE' CHECK (status IN ('PENDENTE', 'PAGO', 'CANCELADO', 'RECUSADO', 'ESTORNADO')),
    metodo_pagamento      VARCHAR(30),
    link_pagamento        VARCHAR(500),
    pago_em               TIMESTAMPTZ,
    criado_em             TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE fotos (
    id          BIGSERIAL PRIMARY KEY,
    empresa_id  BIGINT      NOT NULL REFERENCES empresas (id) ON DELETE CASCADE,
    url         VARCHAR(500) NOT NULL,
    descricao   VARCHAR(255),
    ordem       INT         NOT NULL DEFAULT 0,
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE portfolios (
    id          BIGSERIAL PRIMARY KEY,
    empresa_id  BIGINT      NOT NULL REFERENCES empresas (id) ON DELETE CASCADE,
    titulo      VARCHAR(120),
    descricao   TEXT,
    url_midia   VARCHAR(500),
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE notificacoes (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT      NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    titulo      VARCHAR(150) NOT NULL,
    mensagem    TEXT        NOT NULL,
    lida        BOOLEAN     NOT NULL DEFAULT FALSE,
    tipo        VARCHAR(30),
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notificacoes_usuario ON notificacoes (usuario_id, lida);

CREATE TABLE logs (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT      REFERENCES usuarios (id),
    acao        VARCHAR(100) NOT NULL,
    detalhes    TEXT,
    ip          VARCHAR(45),
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now()
);
