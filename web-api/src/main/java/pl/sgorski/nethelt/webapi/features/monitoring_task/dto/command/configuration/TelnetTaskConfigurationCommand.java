package pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.configuration;

import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.MonitoringTaskConfigurationCommand;

public record TelnetTaskConfigurationCommand(int port, long timeoutMs)
    implements MonitoringTaskConfigurationCommand {}
