package pl.sgorski.nethelt.webapi.features.user.dto.response;

import java.time.Instant;
import java.util.List;

public record DetailedUserResponse(
    Long id,
    String email,
    String role,
    ProfileResponse profile,
    List<UserIdentityResponse> identities,
    boolean isLocal,
    Instant createdAt,
    Instant updatedAt,
    Instant deletedAt) {}
