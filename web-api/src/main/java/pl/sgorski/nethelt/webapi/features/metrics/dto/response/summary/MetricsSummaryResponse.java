package pl.sgorski.nethelt.webapi.features.metrics.dto.response.summary;

import java.util.List;
import org.jspecify.annotations.Nullable;

public record MetricsSummaryResponse(
    long total,
    long successful,
    long failed,
    @Nullable Double uptimePercent,
    List<LatencyStatsResponse> latency) {}
