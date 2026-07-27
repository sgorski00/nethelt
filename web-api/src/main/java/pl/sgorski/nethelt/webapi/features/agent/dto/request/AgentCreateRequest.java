package pl.sgorski.nethelt.webapi.features.agent.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgentCreateRequest(
    @NotBlank(message = "Network agent name cannot be blank")
        @Size(max = 100, message = "Network agent name cannot exceed 100 characters")
        String name) {}
