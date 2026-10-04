package pl.sgorski.nethelt.agent.model.monitoring_result;

import lombok.*;
import org.jspecify.annotations.Nullable;

@Getter
@ToString(callSuper = true)
public final class TelnetResult extends MonitoringResult {

  private final boolean portOpen;

  public TelnetResult(
      Long taskId,
      boolean success,
      String message,
      @Nullable Long responseTimeMs,
      boolean portOpen) {
    super(taskId, success, message, responseTimeMs);
    this.portOpen = portOpen;
  }
}
