package pl.sgorski.nethelt.webapi.features.monitoring_result.dto.command;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record MonitoringResultAddCommand(
    Instant executedAt,
    boolean success,
    @Nullable String message,
    @Nullable Long responseTimeMs,
    MonitoringResultExtensionCommand extension) {}
