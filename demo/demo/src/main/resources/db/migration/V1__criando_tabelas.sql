CREATE TABLE usuario (
    usuario_id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    data_registro DATE
);

CREATE TABLE equipamento (
    equipamento_id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255),
    modelo VARCHAR(255),
    consumo_por_hora DOUBLE PRECISION
);

CREATE TABLE equipamento_usuario (
    equipamento_usuario_id BIGSERIAL PRIMARY KEY,

    usuario_id BIGINT,
    equipamento_id BIGINT,

    nome_identificacao VARCHAR(255),
    horas_por_dia DOUBLE PRECISION,
    consumo_esperado DOUBLE PRECISION,

    CONSTRAINT fk_equipamento_usuario_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(usuario_id),

    CONSTRAINT fk_equipamento_usuario_equipamento
        FOREIGN KEY (equipamento_id)
        REFERENCES equipamento(equipamento_id)
);

CREATE TABLE configuracao (
    id BIGSERIAL PRIMARY KEY,

    usuario_id BIGINT,

    valor_tarifa NUMERIC(10,2),
    meta DOUBLE PRECISION,
    data_fechamento DATE,
    unidade_medida VARCHAR(100),

    CONSTRAINT fk_configuracao_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(usuario_id)
);

CREATE TABLE consumo (
    id BIGSERIAL PRIMARY KEY,

    usuario_id BIGINT,

    data_registro TIMESTAMP,
    consumo_kwh DOUBLE PRECISION NOT NULL,

    CONSTRAINT fk_consumo_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(usuario_id)
);