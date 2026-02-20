package pl.sgorski.nethelt.model;

import lombok.*;

/**
 * Represents the result of a telnet operation on a network device.
 * Extends the generic {@link Result} class to include specific details about the telnet operation.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class TelnetResult extends Result{

  private boolean portOpen;

  public TelnetResult(Device device, boolean success, String message, long responseTimeMs, boolean portOpen) {
    super(device, success, message, responseTimeMs);
    this.portOpen = portOpen;
  }
}
