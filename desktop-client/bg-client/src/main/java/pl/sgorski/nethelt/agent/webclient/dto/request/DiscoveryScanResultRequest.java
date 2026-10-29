package pl.sgorski.nethelt.agent.webclient.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.jspecify.annotations.Nullable;

public record DiscoveryScanResultRequest(
    @NotBlank String ipAddress, @Nullable String hostname, @PositiveOrZero long responseTimeMs) {}
