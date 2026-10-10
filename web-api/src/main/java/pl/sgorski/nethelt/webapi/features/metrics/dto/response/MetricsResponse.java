package pl.sgorski.nethelt.webapi.features.metrics.dto.response;

import java.time.Instant;
import java.util.List;
import pl.sgorski.nethelt.webapi.features.metrics.dto.response.breakdown.TaskMetricsResponse;
import pl.sgorski.nethelt.webapi.features.metrics.dto.response.series.AvailabilityPointResponse;
import pl.sgorski.nethelt.webapi.features.metrics.dto.response.series.LatencyPointResponse;
import pl.sgorski.nethelt.webapi.features.metrics.dto.response.summary.MetricsSummaryResponse;

public record MetricsResponse(
    Instant from,
    Instant to,
    long bucketSeconds,
    MetricsSummaryResponse summary,
    List<AvailabilityPointResponse> availability,
    List<LatencyPointResponse> latency,
    List<TaskMetricsResponse> breakdown) {}
