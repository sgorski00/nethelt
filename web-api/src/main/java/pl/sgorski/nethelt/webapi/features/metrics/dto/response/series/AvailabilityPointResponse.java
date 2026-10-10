package pl.sgorski.nethelt.webapi.features.metrics.dto.response.series;

import java.time.Instant;

public record AvailabilityPointResponse(Instant bucketStart, long total, long successful) {}
