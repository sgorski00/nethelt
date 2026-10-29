package pl.sgorski.nethelt.agent.webclient.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DiscoveryScanAckRequest(@NotBlank String rangeStart, @NotBlank String rangeEnd) {}
