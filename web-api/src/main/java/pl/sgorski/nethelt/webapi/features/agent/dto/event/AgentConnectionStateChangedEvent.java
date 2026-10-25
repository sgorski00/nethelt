package pl.sgorski.nethelt.webapi.features.agent.dto.event;

import pl.sgorski.nethelt.webapi.features.agent.domain.AgentConnectionState;

public record AgentConnectionStateChangedEvent(
    Long userId, Long networkId, Long agentId, AgentConnectionState connectionState) {}
