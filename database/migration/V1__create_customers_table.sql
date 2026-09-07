-- V1__create_customers_table.sql
-- Description: Create customers schema and table for NovaBank

-- 1. Criar o schema customers (se não existir)
CREATE SCHEMA IF NOT EXISTS customers;

-- 2. Criar a tabela dentro do schema customers
CREATE TABLE IF NOT EXISTS customers.customers (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    document_number VARCHAR(50) UNIQUE,
    document_type VARCHAR(20) CHECK (
        document_type IN (
            'PASSPORT',
            'NATIONAL_ID',
            'UK_DRIVING_LICENSE',
            'US_DRIVING_LICENSE',
            'US_SOCIAL_SECURITY',
            'US_STATE_ID'
        )
    ),
    birth_date DATE,
    address TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Índices com schema
CREATE INDEX idx_customers_email ON customers.customers (email);

CREATE INDEX idx_customers_document_number ON customers.customers (document_number);

CREATE INDEX idx_customers_is_active ON customers.customers (is_active);

CREATE INDEX idx_customers_first_name ON customers.customers (first_name);

CREATE INDEX idx_customers_last_name ON customers.customers (last_name);

-- 4. Comentários com schema
COMMENT ON
TABLE customers.customers IS 'Stores customer information for NovaBank';

COMMENT ON COLUMN customers.customers.id IS 'Unique identifier for the customer';

COMMENT ON COLUMN customers.customers.first_name IS 'Customer first name';

COMMENT ON COLUMN customers.customers.middle_name IS 'Customer middle name (optional)';

COMMENT ON COLUMN customers.customers.last_name IS 'Customer last name';

COMMENT ON COLUMN customers.customers.email IS 'Unique email address for login and communication';

COMMENT ON COLUMN customers.customers.phone IS 'Contact phone number';

COMMENT ON COLUMN customers.customers.document_number IS 'KYC document number';

COMMENT ON COLUMN customers.customers.document_type IS 'Type of identification document';

COMMENT ON COLUMN customers.customers.birth_date IS 'Customer date of birth';

COMMENT ON COLUMN customers.customers.address IS 'Customer address';

COMMENT ON COLUMN customers.customers.is_active IS 'Soft delete flag';

COMMENT ON COLUMN customers.customers.created_at IS 'Timestamp of record creation';

COMMENT ON COLUMN customers.customers.updated_at IS 'Timestamp of last update';