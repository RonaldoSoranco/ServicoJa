-- Servico Ja - Categorias iniciais
-- Antes eram criadas apenas pelo seed de desenvolvimento, que nao roda em producao,
-- deixando o ambiente de producao sem nenhuma categoria para o cadastro de empresas.
-- ON CONFLICT preserva bancos que ja possuem essas categorias.

INSERT INTO categorias (nome, descricao, icone) VALUES
    ('Eletricista', 'Servicos eletricos residenciais e comerciais', 'eletricista'),
    ('Encanador', 'Consertos e instalacoes hidraulicas', 'encanador'),
    ('Salão de Beleza', 'Cabelo, unhas e estetica', 'beleza'),
    ('Automecânica', 'Manutencao e reparos de veiculos', 'mecanica'),
    ('Limpeza', 'Limpeza residencial e comercial', 'limpeza'),
    ('Marido de Aluguel', 'Pequenos reparos e instalacoes', 'reparos'),
    ('Costureira', 'Costura, ajustes e bordados', 'costura'),
    ('Designer Gráfico', 'Identidade visual e artes', 'design'),
    ('Professor Particular', 'Aulas particulares de diversas materias', 'educacao'),
    ('Pintor', 'Pintura interna e externa', 'pintura')
ON CONFLICT (nome) DO NOTHING;
