package pl.sgorski.nethelt.webapi.features.user.dto.response;

import java.time.Instant;
import java.time.LocalDate;

public record ProfileResponse(
    Long id,
    String username,
    String firstName,
    String lastName,
    LocalDate birthDate,
    String bio,
    Instant createdAt,
    Instant updatedAt) {}
