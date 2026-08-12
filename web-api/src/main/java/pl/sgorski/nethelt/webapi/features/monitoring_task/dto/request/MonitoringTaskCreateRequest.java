package pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

public record MonitoringTaskCreateRequest(
    @NotNull(message = "Task type must be selected") TaskType type,
    @Positive(message = "Task interval must be greater than 0 seconds") long intervalSeconds) {}
