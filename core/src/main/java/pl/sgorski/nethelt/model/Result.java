package pl.sgorski.nethelt.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Abstract base class representing the result of a network operation on a device.
 * Contains common fields such as device information, timestamp, success status, message, and response time.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Result {

  private Device device;
  private final Instant timestamp = Instant.now();
  private boolean success;
  private String message;
  private long responseTimeMs;

}
