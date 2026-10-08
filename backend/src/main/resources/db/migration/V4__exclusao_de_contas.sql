-- Servico Ja - Exclusao de contas e empresas (LGPD)
-- Contas e empresas excluidas sao anonimizadas em vez de apagadas: assinaturas, pagamentos e
-- registros de acesso precisam ser mantidos por obrigacao legal e fiscal e referenciam essas linhas.

ALTER TABLE usuarios
    ADD COLUMN excluido_em TIMESTAMPTZ;

ALTER TABLE empresas
    ADD COLUMN excluida_em TIMESTAMPTZ;
