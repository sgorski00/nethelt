package pl.sgorski.nethelt.webapi.validator.range;

import java.time.Instant;

/** A time range {@code [from, to)} that can be checked with {@link ValidRange}. */
public interface TimeRange {
  Instant from();

  Instant to();
}
