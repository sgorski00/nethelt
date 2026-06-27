package pl.sgorski.nethelt.webapi.features.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sgorski.nethelt.webapi.exception.domain.ProfileNotFoundException;
import pl.sgorski.nethelt.webapi.features.user.domain.Profile;
import pl.sgorski.nethelt.webapi.features.user.dto.command.ProfileCreateCommand;
import pl.sgorski.nethelt.webapi.features.user.dto.command.ProfileUpdateCommand;
import pl.sgorski.nethelt.webapi.features.user.mapper.ProfileMapper;
import pl.sgorski.nethelt.webapi.features.user.repository.ProfileRepository;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final UserService userService;

    @Transactional
    public Profile createProfile(ProfileCreateCommand command) {
        var user = userService.getUser(command.userId());
        var profile = profileMapper.toProfile(command);
        user.setProfile(profile);
        userService.save(user);
        return profileRepository.findByUserId(command.userId())
                .orElseThrow(() -> new ProfileNotFoundException("Your profile is not found"));
    }

    @Transactional
    public Profile updateProfile(ProfileUpdateCommand command) {
        var existingProfile = profileRepository.findByUserId(command.userId())
                .orElseThrow(() -> new ProfileNotFoundException("Couldn't update the non-existing profile"));
        var user = userService.getUser(command.userId());
        profileMapper.update(existingProfile, command);
        user.setProfile(existingProfile);
        userService.save(user);
        return existingProfile;
    }
}
