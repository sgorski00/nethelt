package pl.sgorski.nethelt.core.model;

/**
 * Represents the result of a telnet operation on a network device.
 * Extends the generic {@link Result} class to include specific details about the telnet operation.
 */
public class TelnetResult extends Result{

  private boolean portOpen;

  public TelnetResult(Device device, boolean success, String message, long responseTimeMs, boolean portOpen) {
    super(device, success, message, responseTimeMs);
    this.portOpen = portOpen;
  }

  public TelnetResult() {}

  public boolean isPortOpen() {
    return portOpen;
  }

  public void setPortOpen(boolean portOpen) {
    this.portOpen = portOpen;
  }
}
