package pl.sgorski.nethelt.agent.webclient.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record MonitoringResultRequest(
    @NotNull Long taskId,
    @NotNull Instant executedAt,
    boolean success,
    @Nullable String message,
    @Nullable @PositiveOrZero Long responseTimeMs,
    @NotNull @Valid MonitoringResultExtensionRequest extension) {}
