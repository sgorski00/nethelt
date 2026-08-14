package pl.sgorski.nethelt.webapi.features.monitoring_task.dto.response;

import java.time.Duration;
import java.time.Instant;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

public record MonitoringTaskResponse(
    Long id,
    TaskType type,
    Duration interval,
    boolean isEnabled,
    Instant createdAt,
    Instant updatedAt) {}
