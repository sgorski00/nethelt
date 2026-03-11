package pl.sgorski.nethelt.webapi.features.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import pl.sgorski.nethelt.webapi.features.user.dto.contract.PasswordChange;
import pl.sgorski.nethelt.webapi.validator.password.ValidPassword;

@ValidPassword
public record PasswordSetRequest(
        @NotBlank String newPassword,
        @NotBlank String repeatNewPassword
) implements PasswordChange { }
