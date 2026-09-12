package pl.sgorski.nethelt.webapi.features.monitoring_task.dto.response;

import java.time.Duration;
import java.time.Instant;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

public record MonitoringTaskClientResponse(
    Long id,
    TaskType type,
    Long deviceId,
    String deviceIp,
    Duration interval,
    boolean isEnabled,
    MonitoringTaskConfigurationResponse configuration,
    Instant updatedAt) {}
