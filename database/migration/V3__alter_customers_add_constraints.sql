-- V3__alter_customers_add_constraints.sql
-- Description: Retrofit integrity/security constraints onto customers
-- (V1 already ran in test/dev — this is a corrective ALTER migration,
-- not an edit to V1, so existing checksums stay valid)

-- 0. Função partilhada de infraestrutura (não é lógica de negócio —
--    só mantém updated_at correto em qualquer UPDATE; vive em public
--    porque vai ser reaproveitada por accounts e futuras tabelas)
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 1. NOT NULL explícito (DEFAULT sozinho não bloqueia um NULL explícito)
ALTER TABLE customers.customers ALTER COLUMN is_active SET NOT NULL;
ALTER TABLE customers.customers ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE customers.customers ALTER COLUMN updated_at SET NOT NULL;

-- 2. Email passa a UNIQUE case-insensitive
--    Se o nome gerado automaticamente por V1 for diferente no teu ambiente,
--    confirma com: SELECT conname FROM pg_constraint WHERE conrelid = 'customers.customers'::regclass;
ALTER TABLE customers.customers DROP CONSTRAINT IF EXISTS customers_email_key;
DROP INDEX IF EXISTS customers.idx_customers_email;
CREATE UNIQUE INDEX uq_customers_email_lower ON customers.customers (LOWER(email));

-- 3. Remover índice redundante (document_number já tem índice automático
--    por ser UNIQUE — o CREATE INDEX explícito em V1 duplicava-o)
DROP INDEX IF EXISTS customers.idx_customers_document_number;

-- 4. Índice de nome: compor em vez de manter os dois separados
DROP INDEX IF EXISTS customers.idx_customers_first_name;
DROP INDEX IF EXISTS customers.idx_customers_last_name;
CREATE INDEX idx_customers_last_first_name ON customers.customers (last_name, first_name);

-- 5. Trigger de updated_at
CREATE TRIGGER trg_customers_updated_at
BEFORE UPDATE ON customers.customers
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- 6. Comentários — só nas colunas que esta migração realmente muda
--    (email: passa a case-insensitive; updated_at: ganha trigger).
--    As restantes colunas mantêm o comentário original de V1, intocado.
COMMENT ON COLUMN customers.customers.email IS 'Unique email address (case-insensitive — see uq_customers_email_lower) for login and communication';
COMMENT ON COLUMN customers.customers.updated_at IS 'Timestamp of last update, maintained by trg_customers_updated_at';
