package pl.sgorski.nethelt.webapi.features.agent.dto.event;

public record AgentOnlineEvent(Long userId, String networkName, String agentName) {}
