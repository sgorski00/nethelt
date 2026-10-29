package pl.sgorski.nethelt.agent.webclient.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DiscoveryScanFailureRequest(@NotBlank String reason) {}
