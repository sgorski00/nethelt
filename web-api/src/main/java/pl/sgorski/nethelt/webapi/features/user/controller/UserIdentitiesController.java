package pl.sgorski.nethelt.webapi.features.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.AuthProvider;
import pl.sgorski.nethelt.webapi.features.user.service.UserService;
import pl.sgorski.nethelt.webapi.security.authenticated.AuthenticatedUserResolver;
import pl.sgorski.nethelt.webapi.security.oauth2.OAuth2ContextCookieService;
import pl.sgorski.nethelt.webapi.security.oauth2.OAuth2ContextService;
import pl.sgorski.nethelt.webapi.security.oauth2.OAuth2Mode;

@Slf4j
@RestController
@RequestMapping(path = "/identities", version = "1")
@RequiredArgsConstructor
public final class UserIdentitiesController {

  private final AuthenticatedUserResolver authenticatedUserResolver;
  private final UserService userService;
  private final OAuth2ContextService oAuth2ContextService;
  private final OAuth2ContextCookieService oAuth2ContextCookieService;

  @PostMapping("/{provider}")
  public ResponseEntity<Void> prepareCookiesForOauthLinking(
      @PathVariable("provider") AuthProvider authProvider, Authentication authentication) {
    log.debug("Linking account with provider: {}", authProvider);
    var userId = authenticatedUserResolver.requireUserId(authentication);
    var token = oAuth2ContextService.generateAccessToken(userId, OAuth2Mode.LINK);
    oAuth2ContextCookieService.writeTokenToResponseSetCookieHeader(token);
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
