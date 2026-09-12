package pl.sgorski.nethelt.agent.model.monitoring_task;

import java.time.Duration;

public record PingTaskConfiguration(Duration timeout) implements MonitoringTaskConfiguration {}
