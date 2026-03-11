ALTER TABLE users ADD COLUMN auth_provider VARCHAR(255) NOT NULL DEFAULT 'LOCAL' check (auth_provider in ('LOCAL', 'GOOGLE'));
ALTER TABLE users ADD COLUMN provider_id VARCHAR(255);

ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;
ALTER TABLE users ADD CONSTRAINT chk_users_password_local
CHECK (
    (auth_provider = 'LOCAL' AND password_hash IS NOT NULL)
    OR auth_provider <> 'LOCAL'
);

ALTER TABLE users ALTER COLUMN username DROP NOT NULL;
ALTER TABLE users ADD CONSTRAINT chk_username_local
CHECK (
    (auth_provider = 'LOCAL' AND username IS NOT NULL)
    OR auth_provider <> 'LOCAL'
);

ALTER TABLE users ADD CONSTRAINT chk_provider_id_local
CHECK (
    (auth_provider <> 'LOCAL' AND users.provider_id IS NOT NULL)
    OR auth_provider = 'LOCAL'
);