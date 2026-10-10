package pl.sgorski.nethelt.webapi.features.metrics.dto.param;

import java.time.Duration;
import java.time.Instant;
import pl.sgorski.nethelt.webapi.validator.range.TimeRange;
import pl.sgorski.nethelt.webapi.validator.range.ValidRange;

/**
 * Time range {@code [from, to)} of the metrics. Bound from optional query parameters, so missing
 * bounds arrive as {@code null} and default to the last {@link #DEFAULT_LENGTH}.
 */
@ValidRange(maxDays = 31)
public record MetricsRange(Instant from, Instant to) implements TimeRange {

  public static final Duration DEFAULT_LENGTH = Duration.ofHours(24);

  public MetricsRange {
    to = to != null ? to : Instant.now();
    from = from != null ? from : to.minus(DEFAULT_LENGTH);
  }

  public Duration length() {
    return Duration.between(from, to);
  }
}
