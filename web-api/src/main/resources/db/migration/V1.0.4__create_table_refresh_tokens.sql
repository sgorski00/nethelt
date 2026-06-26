CREATE TABLE refresh_tokens (
    id bigserial PRIMARY KEY,
    token varchar(255) NOT NULL UNIQUE ,
    user_id bigint NOT NULL references users(id),
    is_revoked bool NOT NULL DEFAULT false,
    expires_at timestamp NOT NULL
);
CREATE INDEX idx_refresh_token_user_id ON refresh_tokens(user_id);