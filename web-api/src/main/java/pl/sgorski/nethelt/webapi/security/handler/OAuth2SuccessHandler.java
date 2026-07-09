package pl.sgorski.nethelt.webapi.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.exception.domain.IdentityNotFoundException;
import pl.sgorski.nethelt.webapi.features.auth.helper.TokenResponseEntityCreator;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.AuthProvider;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.factory.OAuth2UserInfoFactory;
import pl.sgorski.nethelt.webapi.features.user.service.UserIdentityService;

@Slf4j
@Component
@RequiredArgsConstructor
public final class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final UserIdentityService identityService;
  private final TokenResponseEntityCreator tokenResponseEntityCreator;

  @Value("${nh.frontend.oauth-success-url}")
  private String frontendRedirectUrl;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    var oAuth2Token = (OAuth2AuthenticationToken) authentication;
    var provider = AuthProvider.fromString(oAuth2Token.getAuthorizedClientRegistrationId());
    var principal =
        (OAuth2User) Objects.requireNonNull(authentication.getPrincipal(), "Authentication failed");
    var userInfo = OAuth2UserInfoFactory.create(provider, principal.getAttributes());

    try {
      var identity = identityService.findIdentity(userInfo.getProvider(), userInfo.getProviderId());
      var user = identity.getUser();

      var tokenResponse = tokenResponseEntityCreator.createOAuth2Response(user);
      setCookies(response, tokenResponse.getHeaders());
      response.sendRedirect(frontendRedirectUrl);

      log.debug("OAuth2 authentication successful for user: {}", user.getEmail());
    } catch (IdentityNotFoundException ex) {
      log.warn("OAuth2 login blocked: local user with email already exists");
      response.sendError(
          HttpServletResponse.SC_FORBIDDEN, "Local users are not allowed to login with OAuth");
    }
  }

  private void setCookies(HttpServletResponse response, HttpHeaders responseHeaders) {
    responseHeaders
        .getValuesAsList(HttpHeaders.SET_COOKIE)
        .forEach(cookie -> response.addHeader(HttpHeaders.SET_COOKIE, cookie));
  }
}
