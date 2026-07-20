package pl.sgorski.nethelt.webapi.features.auth.dto.event;

public record PasswordResetRequestEvent(Long userId, String resetLink) {}
