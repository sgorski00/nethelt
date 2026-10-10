package pl.sgorski.nethelt.webapi.features.metrics.domain.stats;

import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

/** Results of one task type in one time bucket, numbered from the epoch. */
public record BucketStats(
    Long bucket,
    TaskType type,
    Long total,
    Long successful,
    @Nullable Double avgMs,
    @Nullable Double p95Ms) {}
