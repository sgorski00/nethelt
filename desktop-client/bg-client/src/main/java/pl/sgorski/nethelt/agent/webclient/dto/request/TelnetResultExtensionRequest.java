package pl.sgorski.nethelt.agent.webclient.dto.request;

public record TelnetResultExtensionRequest(boolean portOpen)
    implements MonitoringResultExtensionRequest {}
