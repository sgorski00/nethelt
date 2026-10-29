package pl.sgorski.nethelt.webapi.features.discovery.dto.command;

import org.jspecify.annotations.Nullable;

public record DiscoveryScanResultCommand(
    String ipAddress, @Nullable String hostname, int responseTimeMs) {}
