package pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.configuration;

import jakarta.validation.constraints.Positive;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.MonitoringTaskConfigurationRequest;

public record PingTaskConfigurationRequest(
    @Positive(message = "Timeout must be provided in millis and positive.") long timeoutMs)
    implements MonitoringTaskConfigurationRequest {}
