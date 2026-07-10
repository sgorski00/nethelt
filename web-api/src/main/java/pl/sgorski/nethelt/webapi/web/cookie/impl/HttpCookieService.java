package pl.sgorski.nethelt.webapi.web.cookie.impl;

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
import pl.sgorski.nethelt.webapi.web.cookie.CookieService;

@Service
public class HttpCookieService implements CookieService {

  private static final String SAME_SITE_POLICY = "Lax";
  private static final boolean HTTP_ONLY = true;
  private static final boolean SECURE = true;

  @Override
  public void save(String key, String value, Duration expiration) {
    var response = getCurrentResponse();

    response.addHeader(HttpHeaders.SET_COOKIE, createCookie(key, value, expiration).toString());
  }

  @Override
  public Optional<String> find(String name) {
    var request = getCurrentRequest();
    var cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }
    return Arrays.stream(cookies)
        .filter(cookie -> name.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }

  @Override
  public void delete(String key) {
    var response = getCurrentResponse();

    response.addHeader(HttpHeaders.SET_COOKIE, createCookie(key, "", Duration.ZERO).toString());
  }

  private ResponseCookie createCookie(String key, String value, Duration expiration) {
    return ResponseCookie.from(key, value)
        .httpOnly(HTTP_ONLY)
        .secure(SECURE)
        .sameSite(SAME_SITE_POLICY)
        .path("/")
        .maxAge(expiration)
        .build();
  }

  private HttpServletResponse getCurrentResponse() {
    return Objects.requireNonNull(
        ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse());
  }

  private HttpServletRequest getCurrentRequest() {
    return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
        .getRequest();
  }
}
