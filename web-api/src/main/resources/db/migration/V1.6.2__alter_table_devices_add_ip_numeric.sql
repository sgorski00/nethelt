ALTER TABLE devices ADD COLUMN ip_numeric bigint;

UPDATE devices SET ip_numeric = ip_address - '0.0.0.0'::inet;

ALTER TABLE devices ALTER COLUMN ip_numeric SET NOT NULL;
ALTER TABLE devices ADD CHECK (ip_numeric BETWEEN 0 AND 4294967295);

CREATE INDEX idx_devices_network_id_ip_numeric ON devices (network_id, ip_numeric);
