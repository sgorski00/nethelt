package pl.sgorski.nethelt.model;

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

  public boolean isPortOpen() {
    return portOpen;
  }

  public void setPortOpen(boolean portOpen) {
    this.portOpen = portOpen;
  }

  @Override
  public String toString() {
    return "TelnetResult{" + "portOpen=" + portOpen + ", " + super.toString() + "}";
  }
}
