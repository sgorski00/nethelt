package pl.sgorski.nethelt.webapi.features.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.auth.config.AuthProperties;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2ContextService;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.context.OAuth2Mode;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.userinfo.AuthProvider;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;
import pl.sgorski.nethelt.webapi.security.authenticated.AuthenticatedUserResolver;
import pl.sgorski.nethelt.webapi.web.cookie.CookieNames;
import pl.sgorski.nethelt.webapi.web.cookie.CookieService;

@Slf4j
@RestController
@RequestMapping(path = "/identities", version = "1")
@RequiredArgsConstructor
public final class UserIdentitiesController {

  private final AuthenticatedUserResolver authenticatedUserResolver;
  private final UserService userService;
  private final OAuth2ContextService oAuth2ContextService;
  private final CookieService cookieService;
  private final AuthProperties authProperties;

  @PostMapping("/{provider}")
  public ResponseEntity<Void> prepareCookiesForOauthLinking(
      @PathVariable("provider") AuthProvider authProvider, Authentication authentication) {
    log.debug("Linking account with provider: {}", authProvider);
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var oAuth2CtxCookieExpiration = authProperties.oauth2ContextExpiration();
    var token =
        oAuth2ContextService.generateContextToken(
            userId, OAuth2Mode.LINK, oAuth2CtxCookieExpiration);
    cookieService.save(CookieNames.OAUTH_CONTEXT, token, oAuth2CtxCookieExpiration);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{provider}")
  public ResponseEntity<Void> unlinkOauth2Provider(
      @PathVariable("provider") AuthProvider authProvider, Authentication authentication) {
    log.debug("Unlinking provider: {}", authProvider);
    var userId = authenticatedUserResolver.requireUserId(authentication);
    userService.removeOAuth2AccountLink(userId, authProvider);
    return ResponseEntity.noContent().build();
  }
}
