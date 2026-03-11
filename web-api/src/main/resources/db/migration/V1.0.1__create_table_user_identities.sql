CREATE TABLE user_identities (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL references users(id),
    provider varchar(255) NOT NULL check (provider in ('GOOGLE')),
    provider_id text NOT NULL,
    UNIQUE(provider, provider_id)
);