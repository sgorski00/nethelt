CREATE TABLE discovery_scans (
    id bigserial PRIMARY KEY,
    network_id bigint NOT NULL UNIQUE REFERENCES networks(id) ON DELETE CASCADE,
    status varchar(16) NOT NULL,
    range_start bigint,
    range_end bigint,
    requested_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    failure_reason varchar(255),

    CHECK ((range_start IS NULL) = (range_end IS NULL)),
    CHECK (range_start BETWEEN 0 AND 4294967295),
    CHECK (range_end BETWEEN 0 AND 4294967295),
    CHECK (range_start <= range_end)
);
