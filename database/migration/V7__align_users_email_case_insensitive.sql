-- V7__align_users_email_case_insensitive.sql
-- Description: Align users.email with customers.email semantic (case-insensitive)

-- Verificar duplicados antes:
-- SELECT LOWER(email), COUNT(*) FROM users.users GROUP BY LOWER(email) HAVING COUNT(*) > 1;

ALTER TABLE users.users DROP CONSTRAINT IF EXISTS users_email_key;

DROP INDEX IF EXISTS users.idx_users_email;

CREATE UNIQUE INDEX uq_users_email_lower ON users.users (LOWER(email));

COMMENT ON COLUMN users.users.email IS 'Unique email (case-insensitive — see uq_users_email_lower) used for login';