package pl.sgorski.nethelt.agent.webclient.dto.response;

import java.time.Duration;

public record HttpHealthcheckTaskConfigurationResponse(int port, String path, Duration timeout)
    implements MonitoringTaskConfigurationResponse {}
