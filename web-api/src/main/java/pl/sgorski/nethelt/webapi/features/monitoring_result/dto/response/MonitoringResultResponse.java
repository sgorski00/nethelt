package pl.sgorski.nethelt.webapi.features.monitoring_result.dto.response;

import java.time.Instant;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

public record MonitoringResultResponse(
    Long id,
    TaskType type,
    Instant executedAt,
    boolean success,
    String message,
    Long responseTimeMs,
    MonitoringResultExtensionResponse extension,
    Instant createdAt) {}
