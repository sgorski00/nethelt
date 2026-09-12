package pl.sgorski.nethelt.agent.webclient.dto.response;

import java.time.Duration;

public record TelnetTaskConfigurationResponse(int port, Duration timeout)
    implements MonitoringTaskConfigurationResponse {}
