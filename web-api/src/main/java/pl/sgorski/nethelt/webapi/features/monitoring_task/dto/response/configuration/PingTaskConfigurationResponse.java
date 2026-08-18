package pl.sgorski.nethelt.webapi.features.monitoring_task.dto.response.configuration;

import java.time.Duration;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.response.MonitoringTaskConfigurationResponse;

public record PingTaskConfigurationResponse(Duration timeout)
    implements MonitoringTaskConfigurationResponse {}
