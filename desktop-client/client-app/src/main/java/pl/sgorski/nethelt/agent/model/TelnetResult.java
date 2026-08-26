package pl.sgorski.nethelt.agent.model;

import lombok.*;

@Getter
public final class TelnetResult extends Result {

  private boolean portOpen;

  public TelnetResult(
      Device device, boolean success, String message, long responseTimeMs, boolean portOpen) {
    super(device, success, message, responseTimeMs);
    this.portOpen = portOpen;
  }
}
