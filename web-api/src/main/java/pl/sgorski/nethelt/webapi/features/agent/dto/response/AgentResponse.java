package pl.sgorski.nethelt.webapi.features.agent.dto.response;

import java.time.Instant;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentConnectionState;
import pl.sgorski.nethelt.webapi.features.agent.domain.AgentStatus;

public record AgentResponse(
    Long id,
    String name,
    AgentStatus status,
    AgentConnectionState connectionState,
    @Nullable Instant lastHeartbeatAt,
    Instant tokenCreatedAt,
    AgentConfigurationResponse configuration) {}
