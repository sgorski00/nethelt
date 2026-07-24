package pl.sgorski.nethelt.webapi.features.agent.dto.response;

import java.time.Instant;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentStatus;

public record AgentResponse(
    Long id,
    String name,
    AgentStatus status,
    @Nullable Instant lastHeartbeatAt,
    Instant tokenCreatedAt) {}
