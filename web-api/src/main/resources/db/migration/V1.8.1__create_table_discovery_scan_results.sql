CREATE TABLE discovery_scan_results (
    id bigserial PRIMARY KEY,
    scan_id bigint NOT NULL REFERENCES discovery_scans(id) ON DELETE CASCADE,
    ip_address inet NOT NULL,
    hostname varchar(255),
    response_time_ms integer NOT NULL,

    UNIQUE (scan_id, ip_address),
    CHECK (family(ip_address) = 4),
    CHECK (response_time_ms >= 0)
);
