CREATE TABLE monitoring_task_configurations (
    id BIGSERIAL PRIMARY KEY
);

CREATE TABLE ping_task_configurations (
    id bigserial PRIMARY KEY,
    timeout interval NOT NULL,
    constraint fk_ping_configuration foreign key (id) references monitoring_task_configurations(id) ON DELETE CASCADE
);

CREATE TABLE telnet_task_configurations (
    id bigserial PRIMARY KEY,
    port integer NOT NULL,
    timeout interval NOT NULL,
    constraint fk_telnet_configuration foreign key (id) references monitoring_task_configurations(id) ON DELETE CASCADE
);

CREATE TABLE http_healthcheck_task_configurations (
    id bigserial PRIMARY KEY,
    port integer NOT NULL,
    path varchar(255) NOT NULL,
    timeout interval NOT NULL,
    constraint fk_http_healthcheck_configuration foreign key (id) references monitoring_task_configurations(id) ON DELETE CASCADE
);

ALTER TABLE monitoring_tasks ADD COLUMN configuration_id bigint REFERENCES monitoring_task_configurations(id) ON DELETE CASCADE;
