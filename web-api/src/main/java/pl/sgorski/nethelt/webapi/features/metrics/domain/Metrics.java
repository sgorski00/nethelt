package pl.sgorski.nethelt.webapi.features.metrics.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import pl.sgorski.nethelt.webapi.features.metrics.domain.breakdown.TaskMetrics;
import pl.sgorski.nethelt.webapi.features.metrics.domain.series.AvailabilityPoint;
import pl.sgorski.nethelt.webapi.features.metrics.domain.series.LatencyPoint;
import pl.sgorski.nethelt.webapi.features.metrics.domain.summary.MetricsSummary;

public record Metrics(
    Instant from,
    Instant to,
    Duration bucket,
    MetricsSummary summary,
    List<AvailabilityPoint> availability,
    List<LatencyPoint> latency,
    List<TaskMetrics> breakdown) {}
