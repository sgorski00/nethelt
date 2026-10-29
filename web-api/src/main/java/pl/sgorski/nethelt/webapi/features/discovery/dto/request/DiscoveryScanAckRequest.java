package pl.sgorski.nethelt.webapi.features.discovery.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DiscoveryScanAckRequest(
    @NotBlank(message = "Range start cannot be empty") String rangeStart,
    @NotBlank(message = "Range end cannot be empty") String rangeEnd) {}
