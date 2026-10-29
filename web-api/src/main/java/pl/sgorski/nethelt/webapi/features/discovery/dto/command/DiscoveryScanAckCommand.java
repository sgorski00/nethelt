package pl.sgorski.nethelt.webapi.features.discovery.dto.command;

public record DiscoveryScanAckCommand(Long networkId, String rangeStart, String rangeEnd) {}
