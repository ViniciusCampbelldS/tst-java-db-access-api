-- Execute during the coordinated release, with legacy writers stopped.
-- A header represents one person's delivery; each item is one physical object.
-- Original tables remain available for reconciliation and rollback inspection.
BEGIN;
LOCK TABLE public.epis, public.entregas_epi, public.entrega_epi_itens,
           public.entregas_epis, public.epi_funcionarios IN SHARE ROW EXCLUSIVE MODE;

ALTER TABLE public.entregas_epi
    ALTER COLUMN data_entrega DROP NOT NULL,
    ADD COLUMN IF NOT EXISTS responsavel_id bigint REFERENCES public.funcionarios(id),
    ADD COLUMN IF NOT EXISTS origem_legado_id bigint,
    ADD COLUMN IF NOT EXISTS origem_juncao boolean NOT NULL DEFAULT false;
CREATE UNIQUE INDEX IF NOT EXISTS uk_entrega_origem_legado
    ON public.entregas_epi(origem_legado_id);

ALTER TABLE public.entrega_epi_itens
    ADD COLUMN IF NOT EXISTS encerrado_em timestamptz,
    ADD COLUMN IF NOT EXISTS motivo_encerramento text,
    ADD COLUMN IF NOT EXISTS encerrado_por_id bigint REFERENCES public.funcionarios(id),
    ADD COLUMN IF NOT EXISTS item_anterior_id bigint REFERENCES public.entrega_epi_itens(id);

ALTER TABLE public.epis
    ADD COLUMN IF NOT EXISTS descartado_em timestamptz,
    ADD COLUMN IF NOT EXISTS data_descarte date,
    ADD COLUMN IF NOT EXISTS motivo_descarte text,
    ADD COLUMN IF NOT EXISTS responsavel_descarte_id bigint REFERENCES public.funcionarios(id);

-- Copy legacy flat deliveries only when the same object/person/date is not
-- already represented in the canonical history. No delivery date is invented.
INSERT INTO public.entregas_epi
       (funcionario_id, data_entrega, criada_em, origem_legado_id, origem_juncao)
SELECT l.funcionario_id, l.data_entrega, CURRENT_TIMESTAMP, l.id, false
FROM public.entregas_epis l
WHERE NOT EXISTS (SELECT 1 FROM public.entregas_epi e WHERE e.origem_legado_id = l.id)
  AND NOT EXISTS (
      SELECT 1 FROM public.entrega_epi_itens i
      JOIN public.entregas_epi e ON e.id = i.entrega_id
      WHERE i.epi_id = l.epi_id AND e.funcionario_id = l.funcionario_id
        AND e.data_entrega IS NOT DISTINCT FROM l.data_entrega
  );

INSERT INTO public.entrega_epi_itens(entrega_id, epi_id)
SELECT e.id, l.epi_id
FROM public.entregas_epis l
JOIN public.entregas_epi e ON e.origem_legado_id = l.id
WHERE NOT EXISTS (
    SELECT 1 FROM public.entrega_epi_itens i
    WHERE i.entrega_id = e.id AND i.epi_id = l.epi_id
);

-- A junction does not contain a date. If a matching canonical object/person
-- exists, it adds no event. Otherwise keep an explicit undated legacy header.
INSERT INTO public.entregas_epi
       (funcionario_id, data_entrega, criada_em, origem_juncao)
SELECT DISTINCT j.funcionario_id, NULL::date, CURRENT_TIMESTAMP, true
FROM public.epi_funcionarios j
WHERE NOT EXISTS (
    SELECT 1 FROM public.entrega_epi_itens i
    JOIN public.entregas_epi e ON e.id = i.entrega_id
    WHERE i.epi_id = j.epi_id AND e.funcionario_id = j.funcionario_id
)
AND NOT EXISTS (
    SELECT 1 FROM public.entregas_epi e
    WHERE e.funcionario_id = j.funcionario_id
      AND e.origem_juncao AND e.data_entrega IS NULL
);

INSERT INTO public.entrega_epi_itens(entrega_id, epi_id)
SELECT min(e.id), j.epi_id
FROM (SELECT DISTINCT epi_id, funcionario_id FROM public.epi_funcionarios) j
JOIN public.entregas_epi e
  ON e.funcionario_id = j.funcionario_id AND e.origem_juncao AND e.data_entrega IS NULL
WHERE NOT EXISTS (
    SELECT 1 FROM public.entrega_epi_itens i
    JOIN public.entregas_epi h ON h.id = i.entrega_id
    WHERE i.epi_id = j.epi_id AND h.funcionario_id = j.funcionario_id
)
GROUP BY j.epi_id, j.funcionario_id;

-- Never guess a new owner from a tied date or from an undated association.
-- Compare open candidates only; already closed history is preserved.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.entrega_epi_itens a
        JOIN public.entregas_epi ea ON ea.id = a.entrega_id
        JOIN public.entrega_epi_itens b ON b.epi_id = a.epi_id AND b.id > a.id
        JOIN public.entregas_epi eb ON eb.id = b.entrega_id
        WHERE a.encerrado_em IS NULL AND b.encerrado_em IS NULL
          AND ea.funcionario_id <> eb.funcionario_id
          AND (ea.data_entrega IS NULL OR eb.data_entrega IS NULL
               OR ea.data_entrega = eb.data_entrega)
    ) THEN
        RAISE EXCEPTION 'Conflito de posse: EPI com funcionários distintos na mesma data ou com data desconhecida. Reconcilie os legados antes de migrar.';
    END IF;
END $$;

-- Determine the next distinct dated delivery. Its start of day is an inferred
-- migration boundary, not a claim about the original handover time. Unknown
-- actual times/authors are stated in motivo_encerramento; author remains NULL.
WITH candidatos AS (
    SELECT i.id, i.epi_id, e.data_entrega,
           row_number() OVER (
               PARTITION BY i.epi_id
               ORDER BY e.data_entrega DESC NULLS LAST, i.id DESC
           ) AS ordem
    FROM public.entrega_epi_itens i
    JOIN public.entregas_epi e ON e.id = i.entrega_id
    WHERE i.encerrado_em IS NULL
), encerramentos AS (
    SELECT c.id,
           (SELECT min(h.data_entrega)
            FROM candidatos n
            JOIN public.entrega_epi_itens ni ON ni.id = n.id
            JOIN public.entregas_epi h ON h.id = ni.entrega_id
            WHERE n.epi_id = c.epi_id AND n.data_entrega > c.data_entrega) AS proxima_data
    FROM candidatos c WHERE c.ordem > 1
)
UPDATE public.entrega_epi_itens i
SET encerrado_em = CASE WHEN x.proxima_data IS NOT NULL
                        THEN x.proxima_data::timestamp AT TIME ZONE 'America/Sao_Paulo'
                        ELSE CURRENT_TIMESTAMP END,
    motivo_encerramento = CASE WHEN x.proxima_data IS NOT NULL
        THEN 'MIGRACAO: encerramento inferido pela próxima entrega datada; horário e responsável originais desconhecidos.'
        ELSE 'MIGRACAO: associação duplicada do mesmo funcionário; data e responsável do encerramento originais desconhecidos.' END,
    encerrado_por_id = NULL
FROM encerramentos x WHERE i.id = x.id;

-- A previously substituted/discarded object is unavailable. The migration time
-- records this reconciliation action when the real event timestamp is absent.
UPDATE public.entrega_epi_itens i
SET encerrado_em = COALESCE(e.descartado_em, CURRENT_TIMESTAMP),
    motivo_encerramento = CASE WHEN e.descartado_em IS NOT NULL OR e.data_descarte IS NOT NULL
        THEN 'MIGRACAO: objeto já descartado; vínculo legado encerrado na reconciliação.'
        ELSE 'MIGRACAO: objeto já marcado como substituído; data e responsável originais desconhecidos.' END,
    encerrado_por_id = NULL
FROM public.epis e
WHERE i.epi_id = e.id AND i.encerrado_em IS NULL
  AND (e.substituido OR e.descartado_em IS NOT NULL OR e.data_descarte IS NOT NULL);

CREATE UNIQUE INDEX IF NOT EXISTS uk_epi_entrega_aberta
    ON public.entrega_epi_itens(epi_id) WHERE encerrado_em IS NULL;
CREATE INDEX IF NOT EXISTS idx_entregas_funcionario ON public.entregas_epi(funcionario_id);
CREATE INDEX IF NOT EXISTS idx_entrega_itens_entrega ON public.entrega_epi_itens(entrega_id);
CREATE INDEX IF NOT EXISTS idx_entrega_itens_anterior ON public.entrega_epi_itens(item_anterior_id)
    WHERE item_anterior_id IS NOT NULL;

-- Keep the two old sources as read-only backups, including for the owner-backed
-- Java connection. Disable these triggers deliberately before any rollback.
CREATE OR REPLACE FUNCTION public.bloquear_escrita_legado_epi()
RETURNS trigger LANGUAGE plpgsql SET search_path = pg_catalog AS $$
BEGIN
    RAISE EXCEPTION 'Tabela legada de EPI somente para consulta. Utilize entregas_epi e entrega_epi_itens.';
END $$;
REVOKE ALL ON FUNCTION public.bloquear_escrita_legado_epi() FROM PUBLIC;
DROP TRIGGER IF EXISTS trg_epi_legado_somente_leitura ON public.entregas_epis;
CREATE TRIGGER trg_epi_legado_somente_leitura
    BEFORE INSERT OR UPDATE OR DELETE OR TRUNCATE ON public.entregas_epis
    FOR EACH STATEMENT EXECUTE FUNCTION public.bloquear_escrita_legado_epi();
DROP TRIGGER IF EXISTS trg_epi_juncao_somente_leitura ON public.epi_funcionarios;
CREATE TRIGGER trg_epi_juncao_somente_leitura
    BEFORE INSERT OR UPDATE OR DELETE OR TRUNCATE ON public.epi_funcionarios
    FOR EACH STATEMENT EXECUTE FUNCTION public.bloquear_escrita_legado_epi();

COMMIT;
