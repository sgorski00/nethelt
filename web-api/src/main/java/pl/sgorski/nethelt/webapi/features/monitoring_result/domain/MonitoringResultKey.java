package pl.sgorski.nethelt.webapi.features.monitoring_result.domain;

import java.time.Instant;

public record MonitoringResultKey(Long taskId, Instant executedAt) {

  public static MonitoringResultKey of(MonitoringResult result) {
    return new MonitoringResultKey(result.getTask().getId(), result.getExecutedAt());
  }
}
