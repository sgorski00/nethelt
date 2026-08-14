package pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request;

import jakarta.validation.constraints.Positive;

public record MonitoringTaskUpdateRequest(
    @Positive(message = "Task interval must be greater than 0 seconds") long intervalSeconds) {}
