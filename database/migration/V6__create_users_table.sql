-- V6__create_users_table.sql
-- Passo 1.1: Criar apenas o schema

CREATE SCHEMA IF NOT EXISTS users;

-- Passo 1.2: Criar tabela users
CREATE TABLE IF NOT EXISTS users.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Passo 1.3: Criar tabela user_roles
CREATE TABLE IF NOT EXISTS users.user_roles (
    user_is UUID NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role),
    REFERENCES users.users (id) ON DELETE CASCADE,
    CONSTRAINT chk_user_role CHECK (
        role IN ('ADMIN', 'USER', 'SUPPORT')
    )
);

-- Passo 1.4: Adicionar índices
CREATE INDEX idx_users_email ON users.users (email);

CREATE INDEX idx_user_roles_user_id ON users.user_roles (user_id);

CREATE INDEX idx_users_created_at ON users.users (created_at);