ALTER TABLE network_agents
    ADD COLUMN max_unavailability_time interval NOT NULL DEFAULT interval '5 minutes';
