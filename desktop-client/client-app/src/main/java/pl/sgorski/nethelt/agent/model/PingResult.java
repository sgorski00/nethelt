package pl.sgorski.nethelt.agent.model;

public final class PingResult extends Result {
  public PingResult(Device device, boolean result, String message, long responseTimeMs) {
    super(device, result, message, responseTimeMs);
  }
}
