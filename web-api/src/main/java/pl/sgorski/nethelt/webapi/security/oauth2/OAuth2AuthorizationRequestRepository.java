package pl.sgorski.nethelt.webapi.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.webapi.features.auth.oauth2.config.OAuth2Properties;
import pl.sgorski.nethelt.webapi.web.cookie.CookieNames;
import pl.sgorski.nethelt.webapi.web.cookie.CookieService;

@Component
@RequiredArgsConstructor
public final class OAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  private final CookieService cookieService;
  private final OAuth2Properties oAuth2Properties;

  @Override
  public void saveAuthorizationRequest(
      @Nullable OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      HttpServletResponse response) {
    if (authorizationRequest == null) {
      cookieService.delete(CookieNames.OAUTH_AUTH_REQUEST);
      return;
    }

    cookieService.save(
        CookieNames.OAUTH_AUTH_REQUEST,
        serialize(authorizationRequest),
        oAuth2Properties.authorizationRequestExpiration());
  }

  @Override
  public @Nullable OAuth2AuthorizationRequest removeAuthorizationRequest(
      HttpServletRequest request, HttpServletResponse response) {
    cookieService.delete(CookieNames.OAUTH_AUTH_REQUEST);
    return loadAuthorizationRequest(request);
  }

  @Override
  public @Nullable OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    return cookieService.find(CookieNames.OAUTH_AUTH_REQUEST).map(this::deserialize).orElse(null);
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
