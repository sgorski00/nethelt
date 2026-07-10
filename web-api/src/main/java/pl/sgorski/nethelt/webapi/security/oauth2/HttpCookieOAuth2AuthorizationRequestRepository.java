package pl.sgorski.nethelt.webapi.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public final class HttpCookieOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  private static final String COOKIE_NAME = "oauth2_auth_request";
  private static final Duration COOKIE_EXPIRATION = Duration.ofMinutes(5);
  private static final String SAME_SITE_POLICY = "Lax";
  private static final boolean HTTP_ONLY = true;
  private static final boolean SECURE = true;

  @Override
  public void saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      HttpServletResponse response) {
    if (authorizationRequest == null) {
      clearCookie(response);
      return;
    }

    response.addHeader(
        HttpHeaders.SET_COOKIE,
        createCookie(serialize(authorizationRequest), COOKIE_EXPIRATION).toString());
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(
      HttpServletRequest request, HttpServletResponse response) {
    var authorizationRequest = loadAuthorizationRequest(request);
    clearCookie(response);
    return authorizationRequest;
  }

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    return readCookie(request).map(this::deserialize).orElse(null);
  }

  private void clearCookie(HttpServletResponse response) {
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

  private Optional<String> readCookie(HttpServletRequest request) {
    var cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }

    return Arrays.stream(cookies)
        .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }

  private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
    try (var outputStream = new ByteArrayOutputStream();
        var objectOutputStream = new ObjectOutputStream(outputStream)) {
      objectOutputStream.writeObject(authorizationRequest);
      objectOutputStream.flush();
      return Base64.getUrlEncoder().encodeToString(outputStream.toByteArray());
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to serialize OAuth2 authorization request", ex);
    }
  }

  private OAuth2AuthorizationRequest deserialize(String value) {
    try {
      var decoded = Base64.getUrlDecoder().decode(value);
      try (var inputStream = new ByteArrayInputStream(decoded);
          var objectInputStream = new ObjectInputStream(inputStream)) {
        return (OAuth2AuthorizationRequest) objectInputStream.readObject();
      }
    } catch (IOException | IllegalArgumentException | ClassNotFoundException ex) {
      throw new IllegalStateException("Failed to deserialize OAuth2 authorization request", ex);
    }
  }
}
