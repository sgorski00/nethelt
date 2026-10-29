package pl.sgorski.nethelt.agent.webclient.dto.response;

import org.jspecify.annotations.Nullable;

public record DiscoveryScanResponse(@Nullable String rangeStart, @Nullable String rangeEnd) {}
