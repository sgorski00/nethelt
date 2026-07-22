CREATE TABLE networks (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL references users(id),
    name varchar(255) NOT NULL,
    description text,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);