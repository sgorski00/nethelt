package pl.sgorski.nethelt.webapi.features.user.dto.command;

import java.time.LocalDate;

public record ProfileCreateCommand(
        Long userId,
        String username,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String bio
) {
}
