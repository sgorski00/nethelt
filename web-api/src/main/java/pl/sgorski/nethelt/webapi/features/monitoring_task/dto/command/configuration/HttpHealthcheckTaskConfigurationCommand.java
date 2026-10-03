package pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.configuration;

import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.HttpScheme;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command.MonitoringTaskConfigurationCommand;

public record HttpHealthcheckTaskConfigurationCommand(
    HttpScheme scheme,
    int port,
    String path,
    @Nullable String host,
    @Nullable Integer expectedStatusCode,
    long timeoutMs)
    implements MonitoringTaskConfigurationCommand {}
