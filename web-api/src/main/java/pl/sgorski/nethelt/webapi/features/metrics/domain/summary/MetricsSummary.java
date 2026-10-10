package pl.sgorski.nethelt.webapi.features.metrics.domain.summary;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Totals for the whole selected scope. Latency is kept per task type, because response times of
 * different task types (ping RTT, TCP connect, HTTP request) are not comparable.
 */
public record MetricsSummary(
    long total,
    long successful,
    long failed,
    @Nullable Double uptimePercent,
    List<LatencyStats> latency) {}
