package pl.sgorski.nethelt.webapi.features.user.dto.command;

import java.time.LocalDate;

public record ProfileUpdateCommand(
    Long userId, String firstName, String lastName, LocalDate birthDate, String bio) {}
