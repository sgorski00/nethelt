package pl.sgorski.nethelt.agent.webclient.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AgentAuthRequest(@NotBlank String token) {}
