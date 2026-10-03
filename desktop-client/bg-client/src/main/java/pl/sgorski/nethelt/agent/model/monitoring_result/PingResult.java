package pl.sgorski.nethelt.agent.model.monitoring_result;

import lombok.ToString;

@ToString(callSuper = true)
public final class PingResult extends Result {
  public PingResult(Long taskId, boolean result, String message, long responseTimeMs) {
    super(taskId, result, message, responseTimeMs);
  }
}
