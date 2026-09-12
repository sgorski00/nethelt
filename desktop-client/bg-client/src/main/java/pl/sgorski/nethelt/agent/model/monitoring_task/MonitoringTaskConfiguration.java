package pl.sgorski.nethelt.agent.model.monitoring_task;

public sealed interface MonitoringTaskConfiguration
    permits PingTaskConfiguration, TelnetTaskConfiguration, HttpHealthcheckTaskConfiguration {}
