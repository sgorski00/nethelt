package pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command;

public record MonitoringResultBatchItemCommand(Long taskId, MonitoringResultAddCommand result) {}
