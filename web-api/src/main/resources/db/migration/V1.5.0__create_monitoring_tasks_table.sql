CREATE TABLE monitoring_tasks (
    id bigserial PRIMARY KEY,
    device_id bigint NOT NULL references devices(id) ON DELETE CASCADE,
    type varchar(255) NOT NULL,
    duration interval NOT NULL,
    is_enabled bool NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
);