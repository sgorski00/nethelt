package pl.sgorski.nethelt.webapi.features.metrics.domain.series;

import java.time.Instant;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

public record LatencyPoint(
    Instant bucketStart, TaskType type, @Nullable Double avgMs, @Nullable Double p95Ms) {}
