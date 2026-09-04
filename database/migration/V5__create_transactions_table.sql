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