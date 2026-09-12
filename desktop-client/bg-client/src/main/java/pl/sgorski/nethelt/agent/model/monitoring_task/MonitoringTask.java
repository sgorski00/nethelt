package pl.sgorski.nethelt.agent.model.monitoring_task;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;

public record MonitoringTask(
    Long id,
    Long deviceId,
    InetAddress deviceIp,
    TaskType type,
    Duration interval,
    boolean enabled,
    MonitoringTaskConfiguration configuration,
    Instant updatedAt) {}
