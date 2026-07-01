package pl.sgorski.nethelt.webapi.features.auth.dto.command;

import pl.sgorski.nethelt.webapi.features.user.dto.contract.PasswordChange;

public record RegisterUserCommand(String email, String newPassword, String repeatNewPassword)
    implements PasswordChange {}
