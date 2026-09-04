-- V5__create_transactions_schema.sql
-- Passo 1: Criar apenas o schema

CREATE SCHEMA IF NOT EXISTS transactions;

-- Passo 2: Criar a tabela transactions
CREATE TABLE IF NOT EXISTS transactions.transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts.accounts (id),
    CONSTRAINT chk_transaction_type CHECK (
        transaction_type IN (
            'DEPOSIT',
            'WITHDRAW',
            'TRANSFER_IN',
            'TRANSFER_OUT'
        )
    )
);

-- Passo 3: Adicionar campos complementares
ALTER TABLE transactions.transactions
ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'USD',
ADD COLUMN balance_after DECIMAL(19, 2) NOT NULL,
ADD COLUMN description TEXT,
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;

-- Preencher valores para registros existentes (segurança)
UPDATE transactions.transactions
SET
    balance_after = 0
WHERE
    balance_after IS NULL;

UPDATE transactions.transactions
SET
    currency = 'USD'
WHERE
    currency IS NULL;

-- Aplicar NOT NULL constraints
ALTER TABLE transactions.transactions
ALTER COLUMN currency
SET
    NOT NULL,
ALTER COLUMN balance_after
SET
    NOT NULL,
ALTER COLUMN created_at
SET
    NOT NULL;