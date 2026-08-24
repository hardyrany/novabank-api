-- V1__create_customers_table.sql
-- Description: Create customers table for NovaBank

CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(200),
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

-- Index for performance
CREATE INDEX idx_customers_email ON customers (email);

CREATE INDEX idx_customers_document_number ON customers (document_number);

CREATE INDEX idx_customers_is_active ON customers (is_active);

CREATE INDEX idx_customers_first_name ON customers (first_name);

CREATE INDEX idx_customers_last_name ON customers (last_name);