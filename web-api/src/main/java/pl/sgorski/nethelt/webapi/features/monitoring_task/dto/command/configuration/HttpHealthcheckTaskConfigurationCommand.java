package pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.configuration;

import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.MonitoringTaskConfigurationCommand;

public record HttpHealthcheckTaskConfigurationCommand(int port, String path, long timeoutMs)
    implements MonitoringTaskConfigurationCommand {}
