ALTER TABLE usuario
ALTER COLUMN data_registro TYPE DATE
USING data_registro::DATE;