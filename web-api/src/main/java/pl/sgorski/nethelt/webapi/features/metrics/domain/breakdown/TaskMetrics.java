package pl.sgorski.nethelt.webapi.features.metrics.domain.breakdown;

import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

public record TaskMetrics(
    Long deviceId,
    String deviceName,
    Long taskId,
    TaskType type,
    Long total,
    Long successful,
    Double uptimePercent,
    @Nullable Double avgMs) {}
