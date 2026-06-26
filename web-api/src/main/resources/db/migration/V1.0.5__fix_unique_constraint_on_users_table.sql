ALTER TABLE users DROP CONSTRAINT users_email_deleted_at_key;

CREATE UNIQUE INDEX users_email_unique_active_idx ON users (email) WHERE deleted_at IS NULL;