-- Servico Ja - Melhorias
-- 1) Coluna cpf em usuarios (usada no cadastro de cliente no Asaas).
-- 2) Novos status de assinatura: AGUARDANDO_PAGAMENTO e ATRASADA.
-- 3) Novo status de pagamento: ATRASADO.
-- 4) Indices adicionais para consultas frequentes.

ALTER TABLE usuarios
    ADD COLUMN cpf VARCHAR(14);

ALTER TABLE assinaturas
    DROP CONSTRAINT IF EXISTS assinaturas_status_check;

ALTER TABLE assinaturas
    ADD CONSTRAINT assinaturas_status_check
        CHECK (status IN ('AGUARDANDO_PAGAMENTO', 'ATIVA', 'ATRASADA', 'CANCELADA', 'EXPIRADA'));

ALTER TABLE pagamentos
    DROP CONSTRAINT IF EXISTS pagamentos_status_check;

ALTER TABLE pagamentos
    ADD CONSTRAINT pagamentos_status_check
        CHECK (status IN ('PENDENTE', 'ATRASADO', 'PAGO', 'CANCELADO', 'RECUSADO', 'ESTORNADO'));

CREATE INDEX IF NOT EXISTS idx_assinaturas_empresa_status ON assinaturas (empresa_id, status);
CREATE INDEX IF NOT EXISTS idx_pagamentos_assinatura ON pagamentos (assinatura_id);
CREATE INDEX IF NOT EXISTS idx_pagamentos_asaas ON pagamentos (asaas_pagamento_id);
CREATE INDEX IF NOT EXISTS idx_avaliacoes_empresa_status ON avaliacoes (empresa_id, status);
