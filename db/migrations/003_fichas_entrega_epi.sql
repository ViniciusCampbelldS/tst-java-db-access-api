BEGIN;

CREATE TABLE public.fichas_entrega_epi (
    entrega_id bigint PRIMARY KEY REFERENCES public.entregas_epi(id),
    nome varchar(255) NOT NULL,
    conteudo bytea NOT NULL CHECK (octet_length(conteudo) BETWEEN 5 AND 5242880)
);

-- A API Java é a única interface para consultar o conteúdo das fichas.
ALTER TABLE public.fichas_entrega_epi ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON public.fichas_entrega_epi FROM PUBLIC;
DO $$
DECLARE
    papel text;
BEGIN
    FOREACH papel IN ARRAY ARRAY['anon', 'authenticated'] LOOP
        IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = papel) THEN
            EXECUTE format('REVOKE ALL ON public.fichas_entrega_epi FROM %I', papel);
        END IF;
    END LOOP;
END;
$$;

COMMIT;
