package pl.sgorski.nethelt.agent.model.monitoring_result;

import lombok.ToString;
import org.jspecify.annotations.Nullable;

@ToString(callSuper = true)
public final class PingResult extends MonitoringResult {
  public PingResult(Long taskId, boolean result, String message, @Nullable Long responseTimeMs) {
    super(taskId, result, message, responseTimeMs);
  }
}
