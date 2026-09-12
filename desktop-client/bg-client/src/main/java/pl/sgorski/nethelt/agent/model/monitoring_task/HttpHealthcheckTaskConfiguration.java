package pl.sgorski.nethelt.agent.model.monitoring_task;

import java.time.Duration;

public record HttpHealthcheckTaskConfiguration(int port, String path, Duration timeout)
    implements MonitoringTaskConfiguration {}
