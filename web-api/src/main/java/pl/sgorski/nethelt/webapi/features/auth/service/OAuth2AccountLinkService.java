package pl.sgorski.nethelt.webapi.features.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.features.auth.mapper.AuthMapper;
import pl.sgorski.nethelt.webapi.features.user.service.UserIdentityService;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public final class OAuth2AccountLinkService {

  private final AuthMapper authMapper;
  private final UserService userService;
  private final UserIdentityService userIdentityService;

  public OAuth2User handle(OAuth2LoginContext context) {
    var userInfo = context.userInfo();
    var provider = context.provider();
    var userId = context.linkUserId();

    log.debug("Entering OAuth2 account link mode");
    if (userIdentityService.isUserIdentityPresent(
        userInfo.getProviderId(), userInfo.getProvider())) {
      log.debug("Someone is using account: {} [{}] already.", userInfo.getEmail(), provider.name());
      throw new IllegalStateException("Account is already linked to another user");
    }
    if (userId == null) {
      log.error("There is no OAuth2 link context! Cannot link an oauth2 account");
      throw new IllegalStateException("OAuth2 link context is required to link an account");
    }
    var user = userService.getUserWithProfileAndIdentities(userId);
    log.debug("Linking new identity {} to existing user {}", provider.name(), user.getEmail());
    var identity = authMapper.toIdentity(userInfo);
    user.addIdentity(identity);
    userService.save(user);
    return context.oauthUser();
  }
}
