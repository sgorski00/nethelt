package pl.sgorski.nethelt.webapi.features.agent.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AgentConfigurationUpdateRequest(
    @Min(value = 120, message = "Max unavailability time must be at least 2 minutes")
        @Max(value = 86400, message = "Max unavailability time cannot exceed 24 hours")
        long maxUnavailabilitySeconds) {}
