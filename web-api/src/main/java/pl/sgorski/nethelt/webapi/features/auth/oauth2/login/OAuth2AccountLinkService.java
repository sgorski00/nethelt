package pl.sgorski.nethelt.webapi.features.auth.oauth2.login;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import pl.sgorski.nethelt.webapi.exception.oauth2.AccountAlreadyLinkedException;
import pl.sgorski.nethelt.webapi.exception.oauth2.IncompleteOAuth2DataException;
import pl.sgorski.nethelt.webapi.features.user.service.UserIdentityService;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public final class OAuth2AccountLinkService {

  private final UserService userService;
  private final UserIdentityService userIdentityService;

  // todo: implement new interface with: shouldHandle i handle methods
  public OAuth2User handle(OAuth2LoginContext context) {
    var userInfo = context.userInfo();
    var provider = context.provider();
    var userId = context.linkUserId();

    log.debug("Entering OAuth2 account link mode");
    if (userId == null) {
      log.error("There is no OAuth2 link context! Cannot link an oauth2 account");
      throw new IncompleteOAuth2DataException();
    }
    if (userIdentityService.isUserIdentityPresent(
        userInfo.getProviderId(), userInfo.getProvider())) {
      log.debug("Someone is using account: {} [{}] already.", userInfo.getEmail(), provider.name());
      throw new AccountAlreadyLinkedException();
    }
    var user = userService.getUserWithProfileAndIdentities(userId);
    log.debug("Linking new identity {} to existing user {}", provider.name(), user.getEmail());
    user.addIdentity(userInfo.getProvider(), userInfo.getProviderId());
    userService.save(user);
    return context.oauthUser();
  }
}
