package pl.sgorski.nethelt.webapi.features.monitoring_result.dto.response.extension;

import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.response.MonitoringResultExtensionResponse;

public record TelnetResultExtensionResponse(boolean portOpen)
    implements MonitoringResultExtensionResponse {}
