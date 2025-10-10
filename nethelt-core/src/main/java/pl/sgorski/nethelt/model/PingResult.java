package pl.sgorski.nethelt.model;

/**
 * Represents the result of a ping operation on a network device.
 * Extends the generic {@link Result} class
 */
public class PingResult extends Result {
  public PingResult(Device device, boolean success, String message, long responseTimeMs) {
    super(device, success, message, responseTimeMs);
  }

  @Override
  public String toString() {
    return "PingResult{" + super.toString() +  "}";
  }
}
