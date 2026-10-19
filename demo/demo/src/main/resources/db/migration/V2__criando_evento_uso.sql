-- Tabela que registra cada requisição feita à API (analytics de uso).
-- Cada linha é um "evento" de uso: quem acessou, qual endpoint, quando.
CREATE TABLE evento_uso (
    id BIGSERIAL PRIMARY KEY,

    usuario_id BIGINT,

    endpoint     VARCHAR(255) NOT NULL,
    metodo       VARCHAR(10)  NOT NULL,
    status_http  INT,
    data_hora    TIMESTAMP    NOT NULL,

    CONSTRAINT fk_evento_uso_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(usuario_id)
);

-- Índices para acelerar as consultas de analytics (agregações por endpoint e por data).
CREATE INDEX idx_evento_uso_endpoint  ON evento_uso (endpoint);
CREATE INDEX idx_evento_uso_data_hora ON evento_uso (data_hora);
