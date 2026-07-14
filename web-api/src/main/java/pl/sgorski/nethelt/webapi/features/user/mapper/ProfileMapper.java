package pl.sgorski.nethelt.webapi.features.user.mapper;

import org.mapstruct.Mapper;
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
}
