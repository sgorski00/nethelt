package pl.sgorski.nethelt.webapi.features.monitoring_result.domain;

public record MonitoringResultBatchSummary(int saved, int duplicates, int rejected) {}
