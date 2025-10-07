package pl.sgorski.nethelt.model;

public class NetworkConfig {
  private Operation operation;
  private boolean enabled;
  private int intervalSeconds;

  public NetworkConfig(Operation operation, boolean enabled, int intervalSeconds) {
    this.operation = operation;
    this.enabled = enabled;
    this.intervalSeconds = intervalSeconds;
  }

  public NetworkConfig() {}

  public Operation getOperation() {
    return operation;
  }

  public void setOperation(Operation operation) {
    this.operation = operation;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public int getIntervalSeconds() {
    return intervalSeconds;
  }

  public void setIntervalSeconds(int intervalSeconds) {
    this.intervalSeconds = intervalSeconds;
  }
}
