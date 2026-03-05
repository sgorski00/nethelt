package pl.sgorski.nethelt.webapi.features.user.dto.command;

import pl.sgorski.nethelt.webapi.features.user.dto.contract.PasswordChange;

public record RegisterUserCommand(
        String username,
        String email,
        String password,
        String repeatPassword
) implements PasswordChange { }
