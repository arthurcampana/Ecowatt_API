-- Estende evento_uso para também registrar cliques de interface (UI),
-- não só acessos de API. Mantém as duas fontes na mesma tabela.

-- Tipo do evento: 'API' (requisição interceptada) ou 'CLIQUE' (clique no front).
ALTER TABLE evento_uso ADD COLUMN tipo_evento VARCHAR(10);

-- Rótulo do elemento clicado (ex.: 'logout', 'config', 'salvar'). Null para eventos de API.
ALTER TABLE evento_uso ADD COLUMN rotulo VARCHAR(120);

-- Página/rota do front onde o clique aconteceu (ex.: '/dashboard'). Null para eventos de API.
ALTER TABLE evento_uso ADD COLUMN pagina VARCHAR(255);

-- Os registros que já existem são acessos de API.
UPDATE evento_uso SET tipo_evento = 'API' WHERE tipo_evento IS NULL;

-- A partir de agora o tipo é obrigatório.
ALTER TABLE evento_uso ALTER COLUMN tipo_evento SET NOT NULL;

-- Índices para as consultas de analytics de clique.
CREATE INDEX idx_evento_uso_tipo   ON evento_uso (tipo_evento);
CREATE INDEX idx_evento_uso_rotulo ON evento_uso (rotulo);
