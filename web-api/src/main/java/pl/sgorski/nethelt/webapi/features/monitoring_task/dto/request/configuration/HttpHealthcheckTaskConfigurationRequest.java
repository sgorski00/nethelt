package pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.configuration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.HttpScheme;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.MonitoringTaskConfigurationRequest;

public record HttpHealthcheckTaskConfigurationRequest(
    @NotNull(message = "Scheme must be provided") HttpScheme scheme,
    @Min(value = 1, message = "Port must be greater than 0")
        @Max(value = 65535, message = "Port must be max 65535.")
        int port,
    @NotBlank(message = "Path must be provided") String path,
    @Nullable String host,
    @Min(value = 100, message = "Expected status code must be at least 100.")
        @Max(value = 599, message = "Expected status code must be max 599.")
        @Nullable Integer expectedStatusCode,
    @Positive(message = "Timeout must be provided in millis and positive.") long timeoutMs)
    implements MonitoringTaskConfigurationRequest {}
