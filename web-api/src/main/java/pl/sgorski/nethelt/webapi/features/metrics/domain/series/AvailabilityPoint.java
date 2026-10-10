package pl.sgorski.nethelt.webapi.features.metrics.domain.series;

import java.time.Instant;

public record AvailabilityPoint(Instant bucketStart, long total, long successful) {}
