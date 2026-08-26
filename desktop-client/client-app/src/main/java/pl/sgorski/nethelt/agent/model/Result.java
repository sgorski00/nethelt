package pl.sgorski.nethelt.agent.model;

import java.time.Instant;
import lombok.Getter;

@Getter
public abstract sealed class Result permits PingResult, TelnetResult {
  private Device device;
  private final Instant timestamp = Instant.now();
  private boolean success;
  private String message;
  private long responseTimeMs;

  protected Result(Device device, boolean success, String message, long responseTimeMs) {
    this.device = device;
    this.success = success;
    this.message = message;
    this.responseTimeMs = responseTimeMs;
  }
}
