ALTER TABLE http_healthcheck_task_configurations
    ADD COLUMN expected_status_code integer
        CHECK (expected_status_code BETWEEN 100 AND 599);
