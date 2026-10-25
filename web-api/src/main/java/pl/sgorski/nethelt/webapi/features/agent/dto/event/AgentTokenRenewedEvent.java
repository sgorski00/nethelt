package pl.sgorski.nethelt.webapi.features.agent.dto.event;

public record AgentTokenRenewedEvent(Long userId, String networkName, String agentName) {}
