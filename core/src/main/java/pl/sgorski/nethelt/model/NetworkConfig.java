package pl.sgorski.nethelt.model;

public class NetworkConfig {
  private Operation operation;
  private boolean enabled;
  private int intervalSeconds;

  public NetworkConfig() { } //empty constructor for deserializing - DO NOT REMOVE

  public NetworkConfig(Operation operation, boolean enabled, int intervalSeconds) {
    this.operation = operation;
    this.enabled = enabled;
    this.intervalSeconds = intervalSeconds;
  }

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

  /**
   * Check if the configuration has changed compared to the provided status and interval.
   * If interval is less than 0, it is considered as changed.
   *
   * @param status current enabled status to compare
   * @param interval current interval to compare
   * @return true if the configuration has changed, false otherwise
   */
  public boolean isChanged(boolean status, int interval) {
    if(interval < 0) return true;
    return this.enabled != status || this.intervalSeconds != interval;
  }

  @Override
  public String toString() {
    return "NetworkConfig{" + "operation=" + operation + ", enabled=" + enabled + ", intervalSeconds=" + intervalSeconds + '}';
  }
}
