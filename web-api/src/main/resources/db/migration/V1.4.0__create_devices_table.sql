CREATE TABLE devices (
    id bigserial PRIMARY KEY,
    network_id bigint NOT NULL references networks(id) ON DELETE CASCADE,
    name varchar(255) NOT NULL,
    ip_address inet NOT NULL,
    type varchar(50) NOT NULL,
    is_enabled bool NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (network_id, name),
    UNIQUE (network_id, ip_address),

    CHECK (family(ip_address) = 4)
);