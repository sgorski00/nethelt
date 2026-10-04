package pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.extension;

import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command.MonitoringResultExtensionCommand;

public record TelnetResultExtensionCommand(boolean portOpen)
    implements MonitoringResultExtensionCommand {}
