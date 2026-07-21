CREATE TABLE password_reset_tokens (
    id bigserial PRIMARY KEY,
    token varchar(255) NOT NULL UNIQUE ,
    user_id bigint NOT NULL references users(id),
    is_revoked bool NOT NULL DEFAULT false,
    expires_at timestamp NOT NULL
);