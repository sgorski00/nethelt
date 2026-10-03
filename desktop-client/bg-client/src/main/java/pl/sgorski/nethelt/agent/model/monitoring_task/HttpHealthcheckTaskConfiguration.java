package pl.sgorski.nethelt.agent.model.monitoring_task;

import java.time.Duration;
import org.jspecify.annotations.Nullable;

public record HttpHealthcheckTaskConfiguration(
    HttpScheme scheme,
    int port,
    String path,
    @Nullable String host,
    @Nullable Integer expectedStatusCode,
    Duration timeout)
    implements MonitoringTaskConfiguration {}
