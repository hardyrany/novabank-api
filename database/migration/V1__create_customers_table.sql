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

-- Comments in table and columns
COMMENT ON
TABLE customers IS 'Stores customers information for NovaBank';

COMMENT ON
TABLE customers.id IS 'Unique identifier for the customer';

COMMENT ON COLUMN customers.first_name IS 'Customer first name';

COMMENT ON COLUMN customers.middle_name IS 'Customer middle name (optional)';

COMMENT ON COLUMN customers.last_name IS 'Customer last name';

COMMENT ON COLUMN customers.email IS 'Unique email address for login and communication';

COMMENT ON COLUMN customers.phone IS 'Contact phone number';

COMMENT ON COLUMN customers.document_number IS 'KYC document number';

COMMENT ON COLUMN customers.document_type IS 'Type of identification document: PASSPORT, NATIONAL_ID, UK_DRIVING_LICENSE, US_DRIVING_LICENSE, US_SOCIAL_SECURITY, US_STATE_ID';

COMMENT ON COLUMN customers.birth_date IS 'Customer date of birth';

COMMENT ON COLUMN customers.address IS 'Customer address';

COMMENT ON COLUMN customers.is_active IS 'Soft delete flag';

COMMENT ON COLUMN customers.created_at IS 'Timestamp of record creation';

COMMENT ON COLUMN customers.updated_at IS 'Timestamp of last update';