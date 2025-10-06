package pl.sgorski.nethelt.core.model;

/**
 * Represents the result of a ping operation on a network device.
 * Extends the generic {@link Result} class
 */
public class PingResult extends Result {
  public PingResult(Device device, boolean success, String message, long responseTimeMs) {
    super(device, success, message, responseTimeMs);
  }

  public PingResult() {}
}
