package pl.sgorski.nethelt.webapi.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.web.cookie.CookieService;

@Component
@RequiredArgsConstructor
public final class OAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  private static final String COOKIE_NAME = "oauth2_auth_request";
  private static final Duration COOKIE_EXPIRATION = Duration.ofMinutes(5);

  private final CookieService cookieService;

  @Override
  public void saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      HttpServletResponse response) {
    if (authorizationRequest == null) {
      cookieService.delete(COOKIE_NAME);
      return;
    }

    cookieService.save(COOKIE_NAME, serialize(authorizationRequest), COOKIE_EXPIRATION);
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(
      HttpServletRequest request, HttpServletResponse response) {
    var authorizationRequest = loadAuthorizationRequest(request);
    cookieService.delete(COOKIE_NAME);
    return authorizationRequest;
  }

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    return cookieService.find(COOKIE_NAME).map(this::deserialize).orElse(null);
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
