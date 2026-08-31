-- V2__create_accounts_table.sql
-- Description: Create accounts schema and table for NovaBank

-- 1. Criar o schema accounts (se não existir)
CREATE SCHEMA IF NOT EXISTS accounts;

-- 2. Criar a tabela dentro do schema accounts
CREATE TABLE IF NOT EXISTS accounts.accounts (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    account_Number VARCHAR(20) UNIQUE NOT NULL,
    account_type VARCHAR(20) NOT NULL CHECK (
        account_type IN ('CHECKING', 'SAVINGS')
    ),
    balance DECIMAL(19, 2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'USD',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_customer FOREIGN KEY (customer_id) REFERENCES customers.customers (id)
);

-- 3. Índices para performance
CREATE INDEX idx_accounts_customer_id ON accounts.accounts (customer_id);

CREATE INDEX idx_accounts_accounts_number ON accounts.accounts (account_number);

CREATE INDEX idx_accounts_is_active ON accounts.accounts (is_active);

-- 4. Comentários para documentação
COMMENT ON
TABLE accounts.accounts IS 'Stores customer bank accounts';

COMMENT ON COLUMN accounts.accounts.id IS 'Unique identifier for the account';

COMMENT ON COLUMN accounts.accounts.customer_id IS 'Reference to the customer owning the account';

COMMENT ON COLUMN accounts.accounts.account_number IS 'Unique account number';

COMMENT ON COLUMN accounts.accounts.account_type IS 'Type of account: CHECKING or SAVINGS';

COMMENT ON COLUMN accounts.accounts.balance IS 'Current account balance';

COMMENT ON COLUMN accounts.accounts.currency IS 'Currency code (ISO 4217)';

COMMENT ON COLUMN accounts.accounts.is_active IS 'Soft delete flag';

COMMENT ON COLUMN accounts.accounts.created_at IS 'Timestamp of record creation';

COMMENT ON COLUMN accounts.accounts.updated_at IS 'Timestamp of last update';