package pl.sgorski.nethelt.webapi.features.metrics.dto.response.summary;

import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

public record LatencyStatsResponse(
    TaskType type, long total, @Nullable Double avgMs, @Nullable Double p95Ms) {}
