package pl.sgorski.nethelt.webapi.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.features.auth.helper.TokenResponseEntityCreator;

@Slf4j
@Component
@RequiredArgsConstructor
public final class OAuth2FailureHandler implements AuthenticationFailureHandler {

  private final TokenResponseEntityCreator tokenResponseEntityCreator;

  @Value("${nh.frontend.oauth-failure-url}")
  private String frontendRedirectUrl;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException {
    if (exception instanceof OAuth2AuthenticationException ex) {
      var errorCode = ex.getError().getErrorCode();
      var tokenResponse = tokenResponseEntityCreator.createClearResponse();
      setCookies(response, tokenResponse.getHeaders());
      response.sendRedirect(frontendRedirectUrl + "?error=" + errorCode);
    }
  }

  private void setCookies(HttpServletResponse response, HttpHeaders responseHeaders) {
    responseHeaders
        .getValuesAsList(HttpHeaders.SET_COOKIE)
        .forEach(cookie -> response.addHeader(HttpHeaders.SET_COOKIE, cookie));
  }
}
