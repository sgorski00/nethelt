package pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.configuration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.MonitoringTaskConfigurationRequest;

public record HttpHealthcheckTaskConfigurationRequest(
    @Min(value = 1, message = "Port must be greater than 0")
        @Max(value = 65535, message = "Port must be max 65535.")
        int port,
    @NotBlank(message = "Path must be provided") String path,
    @Positive(message = "Timeout must be provided in millis and positive.") long timeoutMs)
    implements MonitoringTaskConfigurationRequest {}
