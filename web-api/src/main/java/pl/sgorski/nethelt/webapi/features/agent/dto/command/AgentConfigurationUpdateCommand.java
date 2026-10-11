package pl.sgorski.nethelt.webapi.features.agent.dto.command;

public record AgentConfigurationUpdateCommand(Long networkId, long maxUnavailabilitySeconds) {}
