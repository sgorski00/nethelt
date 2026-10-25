package pl.sgorski.nethelt.webapi.features.agent.dto.event;

import pl.sgorski.nethelt.webapi.features.agent.domain.AgentStatus;

public record AgentStatusChangedEvent(
    Long userId, String networkName, String agentName, AgentStatus status) {}
