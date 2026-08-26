package pl.sgorski.nethelt.agent.model;

import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NetworkConfig {

  private Operation operation;
  private boolean enabled;
  private int intervalSeconds;

  /**
   * Check if the configuration has changed compared to the provided status and interval. If
   * interval is less than 0, it is considered as changed.
   *
   * @param status current enabled status to compare
   * @param interval current interval to compare
   * @return true if the configuration has changed, false otherwise
   */
  public boolean isChanged(boolean status, int interval) {
    if (interval < 0) return true;
    return this.enabled != status || this.intervalSeconds != interval;
  }
}
