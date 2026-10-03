ALTER TABLE http_healthcheck_task_configurations
    ADD COLUMN scheme varchar(255) NOT NULL DEFAULT 'HTTP',
    ADD COLUMN host varchar(255);

ALTER TABLE http_healthcheck_task_configurations
    ALTER COLUMN scheme DROP DEFAULT;
