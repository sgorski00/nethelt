package pl.sgorski.nethelt.webapi.features.metrics.domain.stats;

import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

public record TypeStats(
    TaskType type, Long total, Long successful, @Nullable Double avgMs, @Nullable Double p95Ms) {}
