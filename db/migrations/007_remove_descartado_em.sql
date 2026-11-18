-- O descarte passa a ser indicado somente por epis.data_descarte.
UPDATE epis
SET data_descarte = (descartado_em AT TIME ZONE 'America/Sao_Paulo')::date
WHERE descartado_em IS NOT NULL AND data_descarte IS NULL;

ALTER TABLE epis DROP COLUMN IF EXISTS descartado_em;
