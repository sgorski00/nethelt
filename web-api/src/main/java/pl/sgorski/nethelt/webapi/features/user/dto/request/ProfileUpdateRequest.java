package pl.sgorski.nethelt.webapi.features.user.dto.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProfileUpdateRequest(
        String firstName,
        String lastName,
        @Past LocalDate birthDate,
        @Size(max = 255) String bio
) {
}
