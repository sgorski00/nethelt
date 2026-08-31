package pl.sgorski.nethelt.webapi.features.agent.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AgentAuthRequest(@NotBlank String token) {}
