package pl.sgorski.nethelt.webapi.features.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import pl.sgorski.nethelt.webapi.features.user.dto.contract.PasswordChange;
import pl.sgorski.nethelt.webapi.validator.password.ValidPassword;

@ValidPassword
public record RegisterUserRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String repeatPassword
) implements PasswordChange { }
