package pl.sgorski.nethelt.webapi.features.discovery.dto.command;

import org.jspecify.annotations.Nullable;

public record DiscoveryScanStartCommand(Long networkId, @Nullable String ipRange) {}
