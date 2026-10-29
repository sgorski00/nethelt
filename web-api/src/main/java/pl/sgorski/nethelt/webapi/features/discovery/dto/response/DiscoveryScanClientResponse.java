package pl.sgorski.nethelt.webapi.features.discovery.dto.response;

import org.jspecify.annotations.Nullable;

public record DiscoveryScanClientResponse(@Nullable String rangeStart, @Nullable String rangeEnd) {}
