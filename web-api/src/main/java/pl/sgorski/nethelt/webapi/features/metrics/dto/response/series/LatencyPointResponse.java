package pl.sgorski.nethelt.webapi.features.metrics.dto.response.series;

import java.time.Instant;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

public record LatencyPointResponse(
    Instant bucketStart, TaskType type, @Nullable Double avgMs, @Nullable Double p95Ms) {}
