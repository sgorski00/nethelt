package pl.sgorski.nethelt.agent.model.monitoring_result;

import java.time.Instant;
import lombok.Getter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

@Getter
@ToString
public abstract sealed class MonitoringResult
    permits PingResult, TelnetResult, HttpHealthcheckResult {
  private final Long taskId;
  private final Instant timestamp = Instant.now();
  private final boolean success;
  private final String message;
  private final @Nullable Long responseTimeMs;

  protected MonitoringResult(
      Long taskId, boolean success, String message, @Nullable Long responseTimeMs) {
    this.taskId = taskId;
    this.success = success;
    this.message = message;
    this.responseTimeMs = responseTimeMs;
  }
}
