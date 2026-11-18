BEGIN;

-- Turmas passam a referenciar a NR diretamente (turmas_treinamento.nr_id -> nrs.id).
-- Remover treinamento_id também remove as FKs que dependem dele.
ALTER TABLE public.turmas_treinamento DROP COLUMN IF EXISTS treinamento_id;
ALTER TABLE public.turmas_treinamento DROP COLUMN IF EXISTS nome;
-- "documento" duplicava "documento_nome".
UPDATE public.turmas_treinamento SET documento_nome = documento WHERE documento_nome IS NULL AND documento IS NOT NULL;
ALTER TABLE public.turmas_treinamento DROP COLUMN IF EXISTS documento;

DROP TABLE IF EXISTS public.treinamentos;

COMMIT;
