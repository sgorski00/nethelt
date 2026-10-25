package pl.sgorski.nethelt.webapi.features.agent.dto.response;

import pl.sgorski.nethelt.webapi.features.agent.domain.AgentConnectionState;

public record AgentConnectionStateResponse(
    Long networkId, Long agentId, AgentConnectionState connectionState) {}
