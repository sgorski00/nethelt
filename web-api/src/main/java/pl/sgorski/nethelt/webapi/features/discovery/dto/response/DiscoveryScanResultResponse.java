package pl.sgorski.nethelt.webapi.features.discovery.dto.response;

import org.jspecify.annotations.Nullable;

public record DiscoveryScanResultResponse(
    String ipAddress,
    @Nullable String hostname,
    int responseTimeMs,
    @Nullable Long existingDeviceId) {}
