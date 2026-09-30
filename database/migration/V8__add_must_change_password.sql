-- V__add_must_change_password.sql
-- Description: Add must_change_password flag to users table

ALTER TABLE users.users
ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN users.users.must_change_password_password IS 'Forces user to change password on next request (first login or admins)'