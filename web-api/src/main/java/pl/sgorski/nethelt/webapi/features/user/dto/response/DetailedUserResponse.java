package pl.sgorski.nethelt.webapi.features.user.dto.response;

import java.time.Instant;
import java.util.List;

public record DetailedUserResponse(
    String email,
    String role,
    List<UserIdentityResponse> identities,
    Instant createdAt,
    Instant updatedAt,
    Instant deletedAt
) { }
