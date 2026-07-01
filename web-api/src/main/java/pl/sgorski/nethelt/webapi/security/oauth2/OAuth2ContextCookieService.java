package pl.sgorski.nethelt.webapi.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public final class OAuth2ContextCookieService {

  public static final String COOKIE_NAME = "oauth2_ctx";
  private static final Duration COOKIE_EXPIRATION = Duration.ofMinutes(5);
  private static final String SAME_SITE_POLICY = "Lax";
  private static final boolean HTTP_ONLY = true;
  private static final boolean SECURE = true;

  public void writeTokenToResponseSetCookieHeader(String token) {
    var response = getCurrentResponse();
    response.addHeader(HttpHeaders.SET_COOKIE, createCookie(token, COOKIE_EXPIRATION).toString());
  }

  public Optional<String> readOauthContextFromCookies() {
    var request = getCurrentRequest();
    if (request.getCookies() == null) {
      return Optional.empty();
    }

    return Arrays.stream(request.getCookies())
        .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }

  public void clear() {
    var response = getCurrentResponse();
    response.addHeader(HttpHeaders.SET_COOKIE, createCookie("", Duration.ZERO).toString());
  }

  private ResponseCookie createCookie(String value, Duration expiration) {
    return ResponseCookie.from(COOKIE_NAME, value)
        .httpOnly(HTTP_ONLY)
        .secure(SECURE)
        .sameSite(SAME_SITE_POLICY)
        .path("/")
        .maxAge(expiration)
        .build();
  }

  private HttpServletRequest getCurrentRequest() {
    return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
        .getRequest();
  }

  private static HttpServletResponse getCurrentResponse() {
    return Objects.requireNonNull(
        ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse());
  }
}
