package pl.sgorski.nethelt.webapi.features.monitoring_result.dto.response;

public record MonitoringResultBatchAddResponse(int saved, int duplicates, int rejected) {}
