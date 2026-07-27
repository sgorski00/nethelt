package pl.sgorski.nethelt.webapi.features.agent.dto.request;

import jakarta.validation.constraints.NotNull;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentStatus;

public record AgentStatusUpdateRequest(
    @NotNull(message = "Status must be selected") AgentStatus status) {}
