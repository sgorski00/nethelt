package pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.extension;

import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.MonitoringResultExtensionCommand;

public record HttpHealthcheckResultExtensionCommand(@Nullable Integer statusCode)
    implements MonitoringResultExtensionCommand {}
