package pl.sgorski.nethelt.webapi.features.discovery.dto.command;

import java.util.List;

public record DiscoveryScanCompleteCommand(
    Long networkId, List<DiscoveryScanResultCommand> results) {}
