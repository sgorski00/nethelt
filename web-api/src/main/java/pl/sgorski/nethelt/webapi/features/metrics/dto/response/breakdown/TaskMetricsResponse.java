package pl.sgorski.nethelt.webapi.features.metrics.dto.response.breakdown;

import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

public record TaskMetricsResponse(
    Long deviceId,
    String deviceName,
    Long taskId,
    TaskType type,
    long total,
    long successful,
    double uptimePercent,
    @Nullable Double avgMs) {}
