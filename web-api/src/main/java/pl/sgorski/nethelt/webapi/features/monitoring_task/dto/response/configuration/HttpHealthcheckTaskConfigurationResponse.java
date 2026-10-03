package pl.sgorski.nethelt.webapi.features.monitoring_task.dto.response.configuration;

import java.time.Duration;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.HttpScheme;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.response.MonitoringTaskConfigurationResponse;

public record HttpHealthcheckTaskConfigurationResponse(
    HttpScheme scheme,
    int port,
    String path,
    @Nullable String host,
    @Nullable Integer expectedStatusCode,
    Duration timeout)
    implements MonitoringTaskConfigurationResponse {}
