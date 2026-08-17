package pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command;

public record MonitoringTaskUpdateCommand(
    long intervalSeconds, MonitoringTaskConfigurationCommand configuration) {}
