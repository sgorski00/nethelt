package pl.sgorski.nethelt.agent.model.monitoring_result;

import java.time.Instant;
import lombok.Getter;
import pl.sgorski.nethelt.agent.model.Device;

@Getter
public abstract sealed class Result permits PingResult, TelnetResult {
  private final Device device;
  private final Instant timestamp = Instant.now();
  private final boolean success;
  private final String message;
  private final long responseTimeMs;

  protected Result(Device device, boolean success, String message, long responseTimeMs) {
    this.device = device;
    this.success = success;
    this.message = message;
    this.responseTimeMs = responseTimeMs;
  }
}
