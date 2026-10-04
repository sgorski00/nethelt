package pl.sgorski.nethelt.webapi.features.monitoring_result.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record MonitoringResultAddRequest(
    @NotNull(message = "Measurement result must contain associated task id") Long taskId,
    @NotNull(message = "Execution time must be provided") Instant executedAt,
    boolean success,
    @Nullable String message,
    @Nullable @PositiveOrZero(message = "Response time should be greater or equal to 0")
        Long responseTimeMs,
    @NotNull(message = "Measurement result must contain extension data") @Valid
        MonitoringResultExtensionRequest extension) {}
