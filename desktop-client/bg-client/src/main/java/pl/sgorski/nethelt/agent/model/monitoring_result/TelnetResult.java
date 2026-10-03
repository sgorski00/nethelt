package pl.sgorski.nethelt.agent.model.monitoring_result;

import lombok.*;

@Getter
@ToString(callSuper = true)
public final class TelnetResult extends Result {

  private final boolean portOpen;

  public TelnetResult(
      Long taskId, boolean success, String message, long responseTimeMs, boolean portOpen) {
    super(taskId, success, message, responseTimeMs);
    this.portOpen = portOpen;
  }
}
