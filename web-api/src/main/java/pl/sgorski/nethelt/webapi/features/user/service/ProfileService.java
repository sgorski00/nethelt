package pl.sgorski.nethelt.webapi.features.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.ProfileAlreadyExistsException;
import pl.sgorski.nethelt.webapi.exception.domain.ProfileNotFoundException;
import pl.sgorski.nethelt.webapi.features.user.domain.Profile;
import pl.sgorski.nethelt.webapi.features.user.dto.command.ProfileCreateCommand;
import pl.sgorski.nethelt.webapi.features.user.dto.command.ProfileUpdateCommand;
import pl.sgorski.nethelt.webapi.features.user.repository.ProfileRepository;

@Service
@RequiredArgsConstructor
public class ProfileService {

  private final ProfileRepository profileRepository;
  private final UserService userService;

  @Transactional
  public Profile createProfile(ProfileCreateCommand command) {
    var user = userService.getUser(command.userId());
    if (user.getProfile() != null) {
      throw new ProfileAlreadyExistsException();
    }
    var profile =
        new Profile(
            command.username(),
            command.firstName(),
            command.lastName(),
            command.birthDate(),
            command.bio());
    user.setProfile(profile);
    userService.save(user);
    return profile;
  }

  @Transactional
  public Profile updateProfile(ProfileUpdateCommand command) {
    var existingProfile =
        profileRepository
            .findWithUserByUserId(command.userId())
            .orElseThrow(
                () -> new ProfileNotFoundException("Couldn't update the non-existing profile"));
    existingProfile.updatePersonalInformation(
        command.firstName(), command.lastName(), command.birthDate(), command.bio());
    return existingProfile;
  }
}
