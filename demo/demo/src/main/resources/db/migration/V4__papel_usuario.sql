-- Adiciona o papel (role) do usuario para controle de acesso ADMIN x USER.
-- Analytics e mapa de calor passam a ser exclusivos de ADMIN; o restante
-- (inclusive Relatorios) continua disponivel para qualquer usuario logado.

-- 1) Coluna nullable primeiro, para o ADD funcionar com linhas existentes.
ALTER TABLE usuario ADD COLUMN role VARCHAR(20);

-- 2) Backfill: toda linha pre-existente vira USER.
UPDATE usuario SET role = 'USER' WHERE role IS NULL;

-- 3) Default e NOT NULL depois do backfill.
ALTER TABLE usuario ALTER COLUMN role SET DEFAULT 'USER';
ALTER TABLE usuario ALTER COLUMN role SET NOT NULL;

-- 4) Dominio fixado no banco: role so pode ser USER ou ADMIN.
ALTER TABLE usuario ADD CONSTRAINT chk_usuario_role CHECK (role IN ('USER', 'ADMIN'));

-- 5) Seed idempotente do admin padrao, criado automaticamente no primeiro
--    docker compose up (banco novo). Senha = admin123, armazenada como hash
--    BCrypt compativel com o BCryptPasswordEncoder da aplicacao.
INSERT INTO usuario (nome, senha, email, data_registro, role)
VALUES (
    'Administrador',
    '$2a$10$PxxeXTPKwj1571DxJpnqBe5jM69.Gi0EiYy9rhocIWPtHTKpAXcuK',
    'admin@ecowatt.com',
    CURRENT_DATE,
    'ADMIN'
)
ON CONFLICT (email) DO NOTHING;
