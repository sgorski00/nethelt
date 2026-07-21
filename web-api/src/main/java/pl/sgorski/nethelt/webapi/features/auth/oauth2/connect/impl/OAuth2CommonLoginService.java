package pl.sgorski.nethelt.webapi.features.auth.oauth2.connect.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.exception.oauth2.AccountLinkRequiredException;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.connect.OAuth2ConnectService;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2LoginContext;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2Mode;
import pl.sgorski.nethelt.webapi.features.user.domain.User;
import pl.sgorski.nethelt.webapi.features.user.service.UserIdentityService;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public final class OAuth2CommonLoginService implements OAuth2ConnectService {

  private final UserService userService;
  private final UserIdentityService userIdentityService;

  @Override
  public boolean supports(OAuth2Mode mode) {
    return mode == OAuth2Mode.LOGIN;
  }

  @Override
  public OAuth2User handle(OAuth2LoginContext context) {
    var userInfo = context.userInfo();
    var provider = context.provider();
    var oauthUser = context.oauthUser();

    log.debug("Entering OAuth2 login/register mode");
    if (userIdentityService.isUserIdentityPresent(
        userInfo.getProviderId(), userInfo.getProvider())) {
      log.debug(
          "Existing oauth user identity detected: {}, {}. Logging in...",
          userInfo.getEmail(),
          userInfo.getProvider().name());
      return oauthUser;
    }

    if (userService.isUserPresent(userInfo.getEmail())) {
      log.warn("Local user with email {} already exists. OAuthLogin blocked", userInfo.getEmail());
      throw new AccountLinkRequiredException();
    }

    var user = new User(userInfo.getEmail(), userInfo.getProvider(), userInfo.getProviderId());
    log.debug("New user {} created with linked identity {}...", user.getEmail(), provider.name());
    userService.register(user);
    return oauthUser;
  }
}
