package pl.sgorski.nethelt.webapi.features.user.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ProfileCreateRequest(
        @NotNull String username,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String bio
) {
}
