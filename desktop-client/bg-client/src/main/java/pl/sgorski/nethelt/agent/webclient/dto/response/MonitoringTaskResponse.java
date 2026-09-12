package pl.sgorski.nethelt.agent.webclient.dto.response;

import java.time.Duration;
import java.time.Instant;
import pl.sgorski.nethelt.agent.model.monitoring_task.TaskType;

public record MonitoringTaskResponse(
    Long id,
    TaskType type,
    Long deviceId,
    String deviceIp,
    Duration interval,
    boolean isEnabled,
    MonitoringTaskConfigurationResponse configuration,
    Instant updatedAt) {}
