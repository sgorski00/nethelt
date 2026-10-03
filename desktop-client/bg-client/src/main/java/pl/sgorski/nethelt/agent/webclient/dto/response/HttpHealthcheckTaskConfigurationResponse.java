package pl.sgorski.nethelt.agent.webclient.dto.response;

import java.time.Duration;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.agent.model.monitoring_task.HttpScheme;

public record HttpHealthcheckTaskConfigurationResponse(
    HttpScheme scheme,
    int port,
    String path,
    @Nullable String host,
    @Nullable Integer expectedStatusCode,
    Duration timeout)
    implements MonitoringTaskConfigurationResponse {}
