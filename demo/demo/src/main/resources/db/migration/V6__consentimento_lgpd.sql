-- Registro do consentimento aos Termos de Uso e Politica de Privacidade (LGPD).
-- A LGPD (Lei 13.709/2018) exige poder COMPROVAR o consentimento do titular:
-- por isso guardamos quando o usuario aceitou e qual versao dos termos.

-- 1) Colunas nullable primeiro, para o ADD funcionar com usuarios ja existentes.
ALTER TABLE usuario ADD COLUMN termos_aceitos_em TIMESTAMP;
ALTER TABLE usuario ADD COLUMN termos_versao VARCHAR(20);

-- 2) Backfill: usuarios anteriores a esta versao (incluindo o admin de seed)
--    recebem a versao inicial dos termos, com a data da migracao.
UPDATE usuario
   SET termos_aceitos_em = CURRENT_TIMESTAMP,
       termos_versao = '1.0'
 WHERE termos_aceitos_em IS NULL;

-- 3) A partir daqui, todo cadastro grava o aceite; a coluna da data vira NOT NULL.
ALTER TABLE usuario ALTER COLUMN termos_aceitos_em SET NOT NULL;
