-- Servico Ja - Horarios estruturados e desempenho das empresas
-- 1) Horarios de funcionamento por dia da semana (permitem calcular "aberto agora"),
--    substituindo o texto livre horario_funcionamento.
-- 2) Eventos de interacao (visualizacao do perfil e cliques em WhatsApp, ligar e mapa),
--    usados no painel de desempenho da empresa.

CREATE TABLE horarios_funcionamento (
    id          BIGSERIAL PRIMARY KEY,
    empresa_id  BIGINT  NOT NULL REFERENCES empresas (id) ON DELETE CASCADE,
    dia_semana  INT     NOT NULL CHECK (dia_semana BETWEEN 1 AND 7), -- ISO-8601: 1 = segunda ... 7 = domingo
    abre        TIME    NOT NULL,
    fecha       TIME    NOT NULL,
    CONSTRAINT ck_horario_intervalo CHECK (fecha > abre)
);

CREATE INDEX idx_horarios_empresa_dia ON horarios_funcionamento (empresa_id, dia_semana);

ALTER TABLE empresas
    DROP COLUMN horario_funcionamento;

CREATE TABLE eventos_empresa (
    id          BIGSERIAL PRIMARY KEY,
    empresa_id  BIGINT      NOT NULL REFERENCES empresas (id) ON DELETE CASCADE,
    tipo        VARCHAR(30) NOT NULL
        CHECK (tipo IN ('VISUALIZACAO', 'CLIQUE_WHATSAPP', 'CLIQUE_LIGAR', 'CLIQUE_MAPA')),
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_eventos_empresa_periodo ON eventos_empresa (empresa_id, criado_em);
