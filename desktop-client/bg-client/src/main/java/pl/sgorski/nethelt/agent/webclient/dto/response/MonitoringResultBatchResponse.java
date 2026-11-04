package pl.sgorski.nethelt.agent.webclient.dto.response;

public record MonitoringResultBatchResponse(int saved, int duplicates, int rejected) {}
