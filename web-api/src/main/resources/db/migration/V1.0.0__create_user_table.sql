CREATE TABLE users (
    id bigserial PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role varchar(50) NOT NULL check (role in ('USER', 'ADMIN')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE UNIQUE INDEX users_username_unique ON users(username) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX users_email_unique ON users(email) WHERE deleted_at IS NULL;