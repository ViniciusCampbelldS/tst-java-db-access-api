BEGIN;

-- Verifica que nada se perde antes de remover as tabelas legadas.
DO $$
BEGIN
    IF to_regclass('public.dias_notificacao_epi') IS NOT NULL THEN
        IF EXISTS (SELECT 1 FROM public.dias_notificacao_epi r
                   WHERE NOT r.is_norma AND NOT EXISTS (
                       SELECT 1 FROM public.dias_notificacao_epis n
                       WHERE n.ca = trim(r.ca_ou_nr)::integer AND n.dias_aviso = r.dias_aviso)) THEN
            RAISE EXCEPTION 'Regras de CA de dias_notificacao_epi ausentes em dias_notificacao_epis';
        END IF;
        IF EXISTS (SELECT 1 FROM public.dias_notificacao_epi r
                   WHERE r.is_norma AND NOT EXISTS (
                       SELECT 1 FROM public.dias_notificacao_treinamentos n
                       WHERE n.nr = regexp_replace(r.ca_ou_nr, '[^0-9]', '', 'g')::integer AND n.dias_aviso = r.dias_aviso)) THEN
            RAISE EXCEPTION 'Regras de NR de dias_notificacao_epi ausentes em dias_notificacao_treinamentos';
        END IF;
    END IF;

    IF to_regclass('public.entregas_epis') IS NOT NULL AND EXISTS (
        SELECT 1 FROM public.entregas_epis l
        WHERE NOT EXISTS (SELECT 1 FROM public.entregas_epi e WHERE e.origem_legado_id = l.id)) THEN
        RAISE EXCEPTION 'Entregas de entregas_epis ainda não migradas para entregas_epi';
    END IF;

    IF to_regclass('public.epi_funcionarios') IS NOT NULL AND EXISTS (
        SELECT 1 FROM public.epi_funcionarios j
        WHERE NOT EXISTS (
            SELECT 1 FROM public.entrega_epi_itens i JOIN public.entregas_epi e ON e.id = i.entrega_id
            WHERE i.epi_id = j.epi_id AND e.funcionario_id = j.funcionario_id)) THEN
        RAISE EXCEPTION 'Vínculos de epi_funcionarios ainda não migrados para entrega_epi_itens';
    END IF;
END $$;

DROP TABLE IF EXISTS public.dias_notificacao_epi;
DROP TABLE IF EXISTS public.dias_notificacao;
DROP TABLE IF EXISTS public.entregas_epis;
DROP TABLE IF EXISTS public.epi_funcionarios;

-- A ficha fica somente em fichas_entrega_epi.
ALTER TABLE public.entregas_epi
    DROP COLUMN IF EXISTS ficha_nome,
    DROP COLUMN IF EXISTS ficha_tipo_conteudo;

COMMIT;
