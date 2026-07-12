package pl.sgorski.nethelt.webapi.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.web.cookie.CookieNames;
import pl.sgorski.nethelt.webapi.web.cookie.CookieService;

@Slf4j
@Component
@RequiredArgsConstructor
public final class OAuth2FailureHandler implements AuthenticationFailureHandler {

  private final CookieService cookieService;

  @Value("${nh.frontend.oauth-failure-url}")
  private String frontendRedirectUrl;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException {
    if (exception instanceof OAuth2AuthenticationException ex) {
      cookieService.delete(CookieNames.REFRESH_TOKEN);
      var errorCode = ex.getError().getErrorCode();
      response.sendRedirect(frontendRedirectUrl + "?error=" + errorCode);
    }
  }
}
