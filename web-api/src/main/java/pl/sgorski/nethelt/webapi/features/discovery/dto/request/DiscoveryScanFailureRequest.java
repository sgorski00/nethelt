package pl.sgorski.nethelt.webapi.features.discovery.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DiscoveryScanFailureRequest(
    @NotBlank(message = "Failure reason cannot be empty") String reason) {}
