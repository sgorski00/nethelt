package pl.sgorski.nethelt.webapi.features.network.dto.response;

import java.time.Instant;

public record NetworkResponse(Long id, String name, String description, Instant createdAt) {}
