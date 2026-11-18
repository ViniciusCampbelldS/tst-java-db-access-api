BEGIN;

-- Agrupamento de lote passa a ser feito na exibiÃ§Ã£o (mesmo autor, CA e minuto), sem coluna prÃ³pria.
DROP INDEX IF EXISTS public.idx_historico_lote_operacao;
ALTER TABLE public.historico_alteracoes DROP COLUMN IF EXISTS lote_operacao_id;

ALTER TABLE public.historico_comentarios
    ADD COLUMN IF NOT EXISTS criado_em timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- A validade passa a ser da NR; o curso (treinamentos) deixa de ter nome e validade prÃ³prios.
ALTER TABLE public.nrs ADD COLUMN IF NOT EXISTS validade_meses double precision CHECK (validade_meses > 0);

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'public' AND table_name = 'treinamentos' AND column_name = 'validade_meses') THEN
        UPDATE public.nrs n
           SET validade_meses = t.validade_meses
          FROM (SELECT nr_id, MAX(validade_meses) AS validade_meses FROM public.treinamentos GROUP BY nr_id) t
         WHERE n.id = t.nr_id AND n.validade_meses IS NULL;
    END IF;
END $$;

ALTER TABLE public.treinamentos DROP COLUMN IF EXISTS nome;
ALTER TABLE public.treinamentos DROP COLUMN IF EXISTS validade_meses;

COMMIT;
