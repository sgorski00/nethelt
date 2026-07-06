package pl.sgorski.nethelt.webapi.features.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.user.dto.request.ProfileCreateRequest;
import pl.sgorski.nethelt.webapi.features.user.dto.request.ProfileUpdateRequest;
import pl.sgorski.nethelt.webapi.features.user.dto.response.DetailedUserResponse;
import pl.sgorski.nethelt.webapi.features.user.dto.response.ProfileResponse;
import pl.sgorski.nethelt.webapi.features.user.mapper.ProfileMapper;
import pl.sgorski.nethelt.webapi.features.user.mapper.UserMapper;
import pl.sgorski.nethelt.webapi.features.user.service.ProfileService;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;
import pl.sgorski.nethelt.webapi.security.authenticated.AuthenticatedUserResolver;

@RestController
@RequestMapping(path = "/profile", version = "1")
@RequiredArgsConstructor
public final class ProfileController {

  private final UserService userService;
  private final UserMapper userMapper;
  private final ProfileMapper profileMapper;
  private final AuthenticatedUserResolver authenticatedUserResolver;
  private final ProfileService profileService;

  @GetMapping
  public ResponseEntity<DetailedUserResponse> showProfile(Authentication authentication) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var user = userService.getUserWithProfileAndIdentities(userId);
    return ResponseEntity.ok(userMapper.toDetailedResponse(user));
  }

  @PostMapping
  public ResponseEntity<ProfileResponse> createProfile(
      @RequestBody ProfileCreateRequest request, Authentication authentication) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var command = profileMapper.toCreateCommand(userId, request);
    var profile = profileService.createProfile(command);
    return ResponseEntity.status(HttpStatus.CREATED).body(profileMapper.toProfileResponse(profile));
  }

  @PutMapping
  public ResponseEntity<ProfileResponse> updateProfile(
      @RequestBody ProfileUpdateRequest request, Authentication authentication) {
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var command = profileMapper.toUpdateCommand(userId, request);
    var profile = profileService.updateProfile(command);
    return ResponseEntity.ok(profileMapper.toProfileResponse(profile));
  }
}
