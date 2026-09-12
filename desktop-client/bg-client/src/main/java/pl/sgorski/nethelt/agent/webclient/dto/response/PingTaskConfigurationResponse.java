package pl.sgorski.nethelt.agent.webclient.dto.response;

import java.time.Duration;

public record PingTaskConfigurationResponse(Duration timeout)
    implements MonitoringTaskConfigurationResponse {}
