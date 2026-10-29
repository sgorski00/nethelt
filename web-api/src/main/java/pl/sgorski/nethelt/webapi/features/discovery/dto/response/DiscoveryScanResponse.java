package pl.sgorski.nethelt.webapi.features.discovery.dto.response;

import java.time.Instant;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.discovery.domain.DiscoveryScanStatus;

public record DiscoveryScanResponse(
    Long id,
    DiscoveryScanStatus status,
    @Nullable String rangeStart,
    @Nullable String rangeEnd,
    Instant requestedAt,
    @Nullable Instant startedAt,
    @Nullable Instant finishedAt,
    @Nullable String failureReason) {}
