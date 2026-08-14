package pl.sgorski.nethelt.webapi.features.monitoring_task.dto.command;

import pl.sgorski.nethelt.webapi.features.monitoring_task.domain.TaskType;

public record MonitoringTaskCreateCommand(TaskType type, long intervalSeconds) {}
