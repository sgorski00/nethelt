CREATE TABLE monitoring_result_extensions (
    id bigserial PRIMARY KEY
);

CREATE TABLE ping_result_extensions (
    id bigserial PRIMARY KEY,
    constraint fk_ping_result_extension foreign key (id) references monitoring_result_extensions(id) ON DELETE CASCADE
);

CREATE TABLE telnet_result_extensions (
    id bigserial PRIMARY KEY,
    port_open bool NOT NULL,
    constraint fk_telnet_result_extension foreign key (id) references monitoring_result_extensions(id) ON DELETE CASCADE
);

CREATE TABLE http_healthcheck_result_extensions (
    id bigserial PRIMARY KEY,
    status_code integer,
    constraint fk_http_healthcheck_result_extension foreign key (id) references monitoring_result_extensions(id) ON DELETE CASCADE
);

CREATE TABLE monitoring_results (
    id bigserial PRIMARY KEY,
    task_id bigint NOT NULL references monitoring_tasks(id) ON DELETE CASCADE,
    executed_at TIMESTAMPTZ NOT NULL,
    success bool NOT NULL,
    message text,
    response_time_ms bigint,
    monitoring_result_extension_id bigint NOT NULL UNIQUE references monitoring_result_extensions(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_monitoring_results_task_id_executed_at ON monitoring_results(task_id, executed_at);

-- Deleting an extension removes its result through the foreign key above. The opposite direction
-- (a result deleted directly or by the cascade from monitoring_tasks) is handled by this trigger.
-- It is deferred to commit, so when Hibernate deletes the result and then the extension itself,
-- the trigger finds nothing left to delete instead of making Hibernate's delete hit 0 rows.
CREATE FUNCTION delete_monitoring_result_extension() RETURNS trigger AS $$
BEGIN
    DELETE FROM monitoring_result_extensions WHERE id = OLD.monitoring_result_extension_id;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER trg_monitoring_results_delete_extension
    AFTER DELETE ON monitoring_results
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW
    EXECUTE FUNCTION delete_monitoring_result_extension();
