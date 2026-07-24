CREATE TABLE network_agents (
    id bigserial PRIMARY KEY,
    network_id bigint NOT NULL references networks(id) ON DELETE CASCADE,
    name varchar(255) NOT NULL,
    hashed_token text NOT NULL UNIQUE,
    token_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_heartbeat_at TIMESTAMP,
    status varchar(50) NOT NULL DEFAULT 'ACTIVE'
);