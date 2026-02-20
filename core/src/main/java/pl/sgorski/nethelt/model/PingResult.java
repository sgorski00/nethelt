package pl.sgorski.nethelt.model;

import lombok.*;

/**
 * Represents the result of a ping operation on a network device.
 * Extends the generic {@link Result} class
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class PingResult extends Result {
  public PingResult(Device device, boolean pingResult, String message, long responseTime) {
    super(device, pingResult, message, responseTime);
  }
}
