package pl.sgorski.nethelt.webapi.features.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.sgorski.nethelt.webapi.features.user.domain.Profile;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.domain.UserIdentity;
import pl.sgorski.nethelt.webapi.features.user.dto.command.ProfileCreateCommand;
import pl.sgorski.nethelt.webapi.features.user.dto.request.ProfileCreateRequest;
import pl.sgorski.nethelt.webapi.features.user.dto.response.DetailedUserResponse;
import pl.sgorski.nethelt.webapi.features.user.dto.response.ProfileResponse;
import pl.sgorski.nethelt.webapi.features.user.dto.response.UserIdentityResponse;
import pl.sgorski.nethelt.webapi.features.user.dto.response.UserResponse;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileResponse toProfileResponse(Profile profile);
    ProfileCreateCommand toCreateCommand(Long userId, ProfileCreateRequest request);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Profile toProfile(ProfileCreateCommand command);
}
