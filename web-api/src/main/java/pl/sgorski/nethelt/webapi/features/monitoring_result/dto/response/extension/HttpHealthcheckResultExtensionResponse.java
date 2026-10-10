package pl.sgorski.nethelt.webapi.features.monitoring_result.dto.response.extension;

import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.response.MonitoringResultExtensionResponse;

public record HttpHealthcheckResultExtensionResponse(@Nullable Integer statusCode)
    implements MonitoringResultExtensionResponse {}
