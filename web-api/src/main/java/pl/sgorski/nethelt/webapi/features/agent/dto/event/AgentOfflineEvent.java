package pl.sgorski.nethelt.webapi.features.agent.dto.event;

import java.time.Duration;
import java.time.Instant;

public record AgentOfflineEvent(
    Long userId,
    String networkName,
    String agentName,
    Instant lastHeartbeatAt,
    Duration maxUnavailabilityTime) {}
