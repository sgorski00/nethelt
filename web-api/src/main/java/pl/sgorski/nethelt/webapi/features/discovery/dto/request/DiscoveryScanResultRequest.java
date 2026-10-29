package pl.sgorski.nethelt.webapi.features.discovery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record DiscoveryScanResultRequest(
    @NotBlank(message = "IP address cannot be empty") String ipAddress,
    @Nullable @Size(max = 255, message = "Hostname cannot be longer than 255 characters")
        String hostname,
    @NotNull(message = "Response time cannot be empty")
        @PositiveOrZero(message = "Response time cannot be negative")
        Integer responseTimeMs) {}
