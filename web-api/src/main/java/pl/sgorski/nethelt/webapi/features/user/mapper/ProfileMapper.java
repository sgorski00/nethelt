package pl.sgorski.nethelt.webapi.features.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pl.sgorski.nethelt.webapi.features.user.domain.Profile;
import pl.sgorski.nethelt.webapi.features.user.dto.command.ProfileCreateCommand;
import pl.sgorski.nethelt.webapi.features.user.dto.command.ProfileUpdateCommand;
import pl.sgorski.nethelt.webapi.features.user.dto.request.ProfileCreateRequest;
import pl.sgorski.nethelt.webapi.features.user.dto.request.ProfileUpdateRequest;
import pl.sgorski.nethelt.webapi.features.user.dto.response.ProfileResponse;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
  ProfileResponse toProfileResponse(Profile profile);

  ProfileCreateCommand toCreateCommand(Long userId, ProfileCreateRequest request);

  ProfileUpdateCommand toUpdateCommand(Long userId, ProfileUpdateRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  Profile toProfile(ProfileCreateCommand command);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "username", ignore = true)
  void update(@MappingTarget Profile existingProfile, ProfileUpdateCommand command);
}
