-- A origem dos registros migrados já está preservada nos itens e no histórico; as colunas de proveniência não são mais necessárias.
ALTER TABLE public.entregas_epi
    DROP COLUMN IF EXISTS origem_legado_id,
    DROP COLUMN IF EXISTS origem_juncao;
