package pl.sgorski.nethelt.agent.model.monitoring_task;

import java.time.Duration;

public record TelnetTaskConfiguration(int port, Duration timeout)
    implements MonitoringTaskConfiguration {}
