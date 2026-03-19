package pl.sgorski.nethelt.webapi.features.user.dto.response;

public record UserIdentityResponse(
        String provider,
        String providerId
) { }
