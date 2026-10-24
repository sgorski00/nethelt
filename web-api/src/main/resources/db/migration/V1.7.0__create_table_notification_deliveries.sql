CREATE TABLE notification_deliveries (
    id bigserial PRIMARY KEY,
    notification_id bigint NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    channel varchar(32) NOT NULL,
    status varchar(16) NOT NULL,
    attempts integer NOT NULL DEFAULT 0,
    last_error text,
    next_attempt_at TIMESTAMPTZ,
    last_attempt_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_notification_deliveries_notification_channel UNIQUE (notification_id, channel)
);

CREATE INDEX idx_notification_deliveries_due
    ON notification_deliveries (next_attempt_at) WHERE status = 'FAILED';
