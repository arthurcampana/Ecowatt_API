-- Tabela que guarda a previsao de consumo mensal gerada pelo pipeline (Spark).
-- Cada linha e a projecao de consumo (kWh) de um usuario para um mes especifico.
-- ano_mes usa o formato 'YYYY-MM' (ex.: '2025-03'); gerado_em marca quando a
-- previsao foi calculada. id e gerado pelo banco (BIGSERIAL); o Spark NAO insere id.

CREATE TABLE previsao_consumo (
    id BIGSERIAL PRIMARY KEY,

    usuario_id BIGINT,

    ano_mes VARCHAR(7),
    consumo_previsto DOUBLE PRECISION,
    gerado_em TIMESTAMP,

    CONSTRAINT fk_previsao_consumo_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(usuario_id)
);

-- Indice para acelerar a consulta por usuario (GET /previsao/usuario/{usuarioId}).
CREATE INDEX idx_previsao_consumo_usuario ON previsao_consumo(usuario_id);
