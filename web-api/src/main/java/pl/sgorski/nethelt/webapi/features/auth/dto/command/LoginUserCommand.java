package pl.sgorski.nethelt.webapi.features.auth.dto.command;

public record LoginUserCommand(
        String username,
        String password
) {
}
