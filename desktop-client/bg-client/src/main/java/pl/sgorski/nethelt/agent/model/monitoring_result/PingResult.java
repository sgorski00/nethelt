package pl.sgorski.nethelt.agent.model.monitoring_result;

import pl.sgorski.nethelt.agent.model.Device;

public final class PingResult extends Result {
  public PingResult(Device device, boolean result, String message, long responseTimeMs) {
    super(device, result, message, responseTimeMs);
  }
}
