package pl.sgorski.nethelt.webapi.features.discovery.domain;

import java.util.Set;

public enum DiscoveryScanStatus {
  PENDING,
  RUNNING,
  COMPLETED,
  FAILED,
  TIMED_OUT;

  public static final Set<DiscoveryScanStatus> IN_PROGRESS = Set.of(PENDING, RUNNING);
}
