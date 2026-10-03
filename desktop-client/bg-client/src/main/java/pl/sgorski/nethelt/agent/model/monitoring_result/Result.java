package pl.sgorski.nethelt.agent.model.monitoring_result;

import java.time.Instant;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public abstract sealed class Result permits PingResult, TelnetResult {
  private final Long taskId;
  private final Instant timestamp = Instant.now();
  private final boolean success;
  private final String message;
  private final long responseTimeMs;

  protected Result(Long taskId, boolean success, String message, long responseTimeMs) {
    this.taskId = taskId;
    this.success = success;
    this.message = message;
    this.responseTimeMs = responseTimeMs;
  }
}
