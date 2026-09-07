-- V4__alter_accounts_add_constraints.sql
-- Description: Retrofit integrity/security constraints onto accounts
-- (V2 already ran in test/dev — this is a corrective ALTER migration,
-- not an edit to V2, so existing checksums stay valid)

-- 1. NOT NULL explícito
ALTER TABLE accounts.accounts ALTER COLUMN balance SET NOT NULL;
ALTER TABLE accounts.accounts ALTER COLUMN currency SET NOT NULL;
ALTER TABLE accounts.accounts ALTER COLUMN is_active SET NOT NULL;

-- 2. Saldo nunca pode ficar negativo
ALTER TABLE accounts.accounts ADD CONSTRAINT chk_accounts_balance_non_negative CHECK (balance >= 0);

-- 3. Controlo de concorrência (optimistic locking — mapeia para @Version na Entity)
ALTER TABLE accounts.accounts ADD COLUMN version INTEGER NOT NULL DEFAULT 0;

-- 4. FK com ON DELETE explícito (substitui o NO ACTION implícito de V2)
ALTER TABLE accounts.accounts DROP CONSTRAINT fk_account_customer;
ALTER TABLE accounts.accounts ADD CONSTRAINT fk_account_customer
    FOREIGN KEY (customer_id) REFERENCES customers.customers (id) ON DELETE RESTRICT;

-- 5. Remover índice redundante (account_number já tem índice automático
--    por ser UNIQUE — o CREATE INDEX explícito em V2 duplicava-o)
DROP INDEX IF EXISTS accounts.idx_accounts_accounts_number;

-- 6. Trigger de updated_at (reaproveita a função criada em V3)
CREATE TRIGGER trg_accounts_updated_at
BEFORE UPDATE ON accounts.accounts
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- 7. Comentários novos/atualizados
COMMENT ON COLUMN accounts.accounts.balance IS 'Current account balance; must never be negative';
COMMENT ON COLUMN accounts.accounts.customer_id IS 'Reference to the customer owning the account (ON DELETE RESTRICT: cannot delete a customer with existing accounts)';
COMMENT ON COLUMN accounts.accounts.version IS 'Optimistic locking counter (maps to JPA @Version) — prevents lost updates under concurrent deposit/withdraw/transfer';
COMMENT ON COLUMN accounts.accounts.updated_at IS 'Timestamp of last update, maintained by trg_accounts_updated_at';
