package pl.sgorski.nethelt.model;

import java.time.Instant;

/**
 * Abstract base class representing the result of a network operation on a device.
 * Contains common fields such as device information, timestamp, success status, message, and response time.
 */
public abstract class Result {
  private Device device;
  private final Instant timestamp = Instant.now();
  private boolean success;
  private String message;
  private long responseTimeMs;

  protected Result(Device device, boolean success, String message, long responseTimeMs) {
    this.device = device;
    this.success = success;
    this.message = message;
    this.responseTimeMs = responseTimeMs;
  }

  public Result() {}

  public Instant getTimestamp() {
    return timestamp;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getMessage() {
    return message;
  }

  public Device getDevice() {
    return device;
  }

  public long getResponseTimeMs() {
    return responseTimeMs;
  }

  public void setDevice(Device device) {
    this.device = device;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setResponseTimeMs(long responseTimeMs) {
    this.responseTimeMs = responseTimeMs;
  }

  @Override
  public String toString() {
    return "Result{" + "device=" + device + ", timestamp=" + timestamp + ", success=" + success + ", message='"
      + message + '\'' + ", responseTimeMs=" + responseTimeMs + '}';
  }
}
