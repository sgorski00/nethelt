package pl.sgorski.nethelt.webapi.features.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.sgorski.nethelt.webapi.features.user.domain.Profile;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.domain.UserIdentity;
import pl.sgorski.nethelt.webapi.features.user.dto.response.DetailedUserResponse;
import pl.sgorski.nethelt.webapi.features.user.dto.response.ProfileResponse;
import pl.sgorski.nethelt.webapi.features.user.dto.response.UserIdentityResponse;
import pl.sgorski.nethelt.webapi.features.user.dto.response.UserResponse;

@Mapper(componentModel = "spring", uses = ProfileMapper.class)
public interface UserMapper {
    @Mapping(target = "role", expression = "java(user.getRole().getDisplayName())")
    UserResponse toResponse(User user);

    @Mapping(target = "role", expression = "java(user.getRole().getDisplayName())")
    DetailedUserResponse toDetailedResponse(User user);

    @Mapping(target = "provider", expression = "java(identity.getProvider().name())")
    UserIdentityResponse toIdentityResponse(UserIdentity identity);
}
